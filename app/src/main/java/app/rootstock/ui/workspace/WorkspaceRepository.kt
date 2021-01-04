package app.rootstock.ui.workspace

import androidx.room.withTransaction
import app.rootstock.api.WorkspaceService
import app.rootstock.data.channel.ChannelDao
import app.rootstock.data.db.AppDatabase
import app.rootstock.data.network.NetworkBoundRepository
import app.rootstock.data.network.ResponseResult
import app.rootstock.data.prefs.CacheClass
import app.rootstock.data.prefs.SharedPrefsController
import app.rootstock.data.workspace.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface WorkspaceRepository {

    /**
     * returns workspace by id either from local storage or network
     */
    suspend fun getWorkspace(
        workspaceId: String,
        update: Boolean
    ): Flow<ResponseResult<WorkspaceWithChildren?>>

    /**
     * sends a DELETE request for @param workspaceId and removes from local storage
     */
    suspend fun deleteWorkspace(workspaceId: String, parentId: String): Flow<ResponseResult<Void?>>

    suspend fun createWorkspace(createWorkspace: CreateWorkspaceRequest): Flow<ResponseResult<Workspace?>>

    suspend fun updateWorkspace(workspace: WorkspaceI, workspaceId: String): Flow<ResponseResult<Workspace?>>
}

class WorkspaceRepositoryImpl @Inject constructor(
    private val workspaceRemoteSource: WorkspaceService,
    private val workspaceLocal: WorkspaceDao,
    private val channelLocal: ChannelDao,
    private val spController: SharedPrefsController,
    private val database: AppDatabase,
) : WorkspaceRepository {


    @ExperimentalCoroutinesApi
    override suspend fun getWorkspace(workspaceId: String, update: Boolean) =
        object : NetworkBoundRepository<WorkspaceWithChildren?, WorkspaceWithChildren>() {
            override suspend fun persistData(response: WorkspaceWithChildren) {
                response
                    .let {
                        database.withTransaction {
                            // insert workspace
                            workspaceLocal.upsert(
                                Workspace(
                                    name = it.name,
                                    workspaceId = it.workspaceId,
                                    backgroundColor = it.backgroundColor,
                                    imageUrl = it.imageUrl,
                                    createdAt = it.createdAt,
                                )
                            )
                            // insert children workspaces
                            workspaceLocal.upsertAll(it.children)

                            // insert channels
                            channelLocal.upsertAll(it.channels)

                            // create hierarchy
                            val tree = mutableListOf<WorkspaceTree>()
                            it.children.forEach { child ->
                                tree.add(
                                    WorkspaceTree(
                                        parent = it.workspaceId,
                                        child = child.workspaceId
                                    )
                                )
                            }
                            workspaceLocal.insertHierarchy(tree)
                        }
                    }
            }

            override suspend fun fetchFromLocal(): Flow<WorkspaceWithChildren?> {
                val workspaces = workspaceLocal.getChildrenWorkspacesById(workspaceId).sortedBy {
                    it.createdAt
                }
                val workspaceWithChannels =
                    workspaceLocal.getWorkspaceWithChannels(workspaceId)
                        ?: return flow { emit(null) }
                return flow {
                    emit(
                        WorkspaceWithChildren(
                            workspaceId = workspaceWithChannels.workspace.workspaceId,
                            name = workspaceWithChannels.workspace.name,
                            imageUrl = workspaceWithChannels.workspace.imageUrl,
                            backgroundColor = workspaceWithChannels.workspace.backgroundColor,
                            children = workspaces.toMutableList(),
                            channels = workspaceWithChannels.channels,
                            createdAt = workspaceWithChannels.workspace.createdAt,
                        )
                    )
                }
            }

            override suspend fun fetchFromRemote(): WorkspaceWithChildren? {
                val cacheControl = if (update) "no-cache" else null
                val workspaceResponse =
                    workspaceRemoteSource.getWorkspace(workspaceId, cacheControl)
                return workspaceResponse.body()
            }
        }.asFlow()


    override suspend fun deleteWorkspace(workspaceId: String, parentId: String): Flow<ResponseResult<Void?>> = flow {
        val channelResponse =
            workspaceRemoteSource.deleteWorkspace(workspaceId)

        val state = when (channelResponse.isSuccessful) {
            true -> {
                ResponseResult.success(channelResponse.body())
            }
            else -> ResponseResult.error(channelResponse.message())
        }
        if (channelResponse.isSuccessful) {
            workspaceLocal.delete(workspaceId)
            spController.updateCacheSettings(CacheClass.Workspace(parentId), true)
        }
        emit(state)

    }.catch {
        emit(ResponseResult.error("Unable to delete workspace"))
    }

    override suspend fun createWorkspace(createWorkspace: CreateWorkspaceRequest): Flow<ResponseResult<Workspace?>> =
        flow {
            val response =
                workspaceRemoteSource.createWorkspace(createWorkspace)

            val state = when (response.isSuccessful) {
                true -> {
                    ResponseResult.success(response.body())
                }
                else -> ResponseResult.error(response.message())
            }
            if (response.isSuccessful) {
                createWorkspace.workspaceId?.let {
                    spController.updateCacheSettings(CacheClass.Workspace(it), true)
                } ?: return@flow
                workspaceLocal.insert(response.body())
            }
            emit(state)
        }.catch {
            emit(ResponseResult.error("Unable to create workspace"))
        }


    override suspend fun updateWorkspace(workspace: WorkspaceI, workspaceId: String): Flow<ResponseResult<Workspace?>> =
        flow {
            val request =
                UpdateWorkspaceRequest(name = workspace.name, imageUrl = workspace.imageUrl)
            val response =
                workspaceRemoteSource.updateWorkspace(
                    workspaceId = workspace.workspaceId,
                    workspaceRequest = request
                )

            val state = when (response.isSuccessful) {
                true -> {
                    ResponseResult.success(response.body())
                }
                else -> ResponseResult.error(response.message())
            }
            if (response.isSuccessful) {
                workspaceLocal.update(workspace = response.body())
                spController.updateCacheSettings(CacheClass.Workspace(workspace.workspaceId), true)
                spController.updateCacheSettings(CacheClass.Workspace(workspaceId), true)
            }
            emit(state)
        }.catch {
            emit(ResponseResult.error("Unable to update workspace"))
        }


}

