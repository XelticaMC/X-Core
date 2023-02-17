package work.xeltica.craft.core.hooks

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import work.xeltica.craft.core.api.HookBase

object NotionHook : HookBase() {
    override val isEnabled: Boolean
        get() = _isEnabled

    private var _isEnabled = false
    private lateinit var notionApiKey: String

    private const val NOTION_API_VERSION = "2022-06-28"
    private const val NOTION_API_ENDPOINT = "https://api.notion.com/v1/"

    fun insertDB(databaseId: String, fields: Map<String, Any>) {
        Gson().toJson()
        request("pages")
            .post("".toRequestBody("application/json".toMediaType()))
    }


    fun request(path: String) = Request.Builder()
        .url("$NOTION_API_ENDPOINT$path")
        .addHeader("Accept", "application/json")
        .addHeader("Notion-Version", NOTION_API_VERSION)
        .addHeader("Authorization", "Bearer $notionApiKey")
}