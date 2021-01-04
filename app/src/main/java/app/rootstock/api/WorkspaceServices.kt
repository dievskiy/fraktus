package app.rootstock.api

import app.rootstock.data.workspace.*
import retrofit2.Response
import retrofit2.http.*


interface WorkspaceService {
    @GET("/workspaces/{workspaceId}")
    suspend fun getWorkspace(
        @Path("workspaceId") workspaceId: String,
        @Header("Cache-Control") cacheControl: String? = null,
    ): Response<WorkspaceWithChildren>

    @DELETE("/workspaces/{workspaceId}")
    suspend fun deleteWorkspace(
        @Path("workspaceId") workspaceId: String
    ): Response<Void>

    @POST("/workspaces/")
    suspend fun createWorkspace(
        @Body workspaceRequest: CreateWorkspaceRequest
    ): Response<Workspace>

    @PATCH("/workspaces/{workspaceId}")
    suspend fun updateWorkspace(
        @Path("workspaceId") workspaceId: String,
        @Body workspaceRequest: UpdateWorkspaceRequest
    ): Response<Workspace>

}