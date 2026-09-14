package ai.rever.boss.plugin.dynamic.bookmarks.manager

import ai.rever.boss.plugin.bookmark.FavoriteWorkspace
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BookmarkSerializerTest {
    @Test
    fun `legacy documents with omitted defaults remain readable`() {
        val legacy = """[{"id":"legacy","name":"Old","bookmarks":[{"id":"bookmark","tabConfig":{"type":"browser","title":"Old tab"},"workspaceName":"Work"}]}]"""
        val loaded = BookmarkSerializer.deserializeCollections(legacy)
        assertEquals("legacy", loaded.single().id)
        assertEquals("Work", loaded.single().bookmarks.single().workspaceName)
        assertTrue(loaded.single().bookmarks.single().tags.isEmpty())
        assertEquals(loaded, BookmarkSerializer.deserializeCollections(BookmarkSerializer.serializeCollections(loaded)))
    }

    @Test
    fun `shared serializer persists favorite workspace timestamp and reads legacy favorites`() {
        val favorite = FavoriteWorkspace(workspaceId = "work", workspaceName = "Work")
        val encoded = BookmarkSerializer.serializeFavoriteWorkspaces(listOf(favorite))
        val saved = Json.parseToJsonElement(encoded).jsonArray.single().jsonObject
        assertEquals(favorite.markedAt.toString(), saved.getValue("markedAt").jsonPrimitive.content)
        assertEquals(listOf(favorite), BookmarkSerializer.deserializeFavoriteWorkspaces(encoded))
        val legacy = BookmarkSerializer.deserializeFavoriteWorkspaces("""[{"workspaceId":"work","workspaceName":"Work"}]""")
        assertEquals("work", legacy.single().workspaceId)
        assertEquals(legacy, BookmarkSerializer.deserializeFavoriteWorkspaces(BookmarkSerializer.serializeFavoriteWorkspaces(legacy)))
    }
}
