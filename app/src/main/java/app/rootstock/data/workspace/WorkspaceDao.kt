package app.rootstock.data.workspace

import androidx.room.*


@Dao
interface WorkspaceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(workspace: Workspace?): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(workspace: Workspace?)

    @Transaction
    suspend fun upsert(workspace: Workspace?) {
        val id = insert(workspace)
        if (id == -1L) {
            update(workspace)
        }
    }

    @Transaction
    @Query("select * from workspaces where ws_id = :id limit 1")
    suspend fun getWorkspaceWithChannels(id: String): WorkspaceWithChannels?

    @Query("select * from workspaces where ws_id in (select child from workspaces_tree where parent = :id);")
    suspend fun getChildrenWorkspacesById(id: String): List<Workspace>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    @JvmSuppressWildcards
    @Transaction
    suspend fun insertHierarchy(hierarchy: List<WorkspaceTree>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<Workspace?>?)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(entities: List<Workspace?>?)

    @Transaction
    suspend fun upsertAll(entities: List<Workspace?>?) {
        insertAll(entities)
        update(entities)
    }

    /**
     * Note. Channel will be deleted automatically due to CASCADE deletion
     */
    @Transaction
    suspend fun delete(workspaceId: String) {
        val childrenIds = getChildrenIds(workspaceId).toSet().toList()
        // delete all hierarchical relations
        deleteWorkspaceTree(workspaceId)
        deleteWorkspace(workspaceId)
        // delete all children workspaces
        deleteWorkspaces(childrenIds)
    }

    @Query("delete from workspaces where ws_id = :workspaceId")
    suspend fun deleteWorkspace(workspaceId: String)

    @Query("select child from workspaces_tree where parent = :workspaceId")
    suspend fun getChildrenIds(workspaceId: String): List<String>

    @Query("delete from workspaces where ws_id in (:ids)")
    suspend fun deleteWorkspaces(ids: List<String>)

    @Query("delete from workspaces_tree where parent = :workspaceId or child = :workspaceId")
    suspend fun deleteWorkspaceTree(workspaceId: String)

    @Transaction
    suspend fun deleteAll(){
        deleteAllWorkspaceTrees()
        deleteAllWorkspaces()
    }

    @Query("delete from workspaces")
    suspend fun deleteAllWorkspaces()

    @Query("delete from workspaces_tree")
    suspend fun deleteAllWorkspaceTrees()

}