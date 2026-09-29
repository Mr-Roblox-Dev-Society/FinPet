package ru.finny.pet.content

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.json.JSONArray
import org.json.JSONObject
import ru.finny.pet.domain.model.ClothingItemDefinition
import ru.finny.pet.domain.model.ClothingVariantDefinition
import ru.finny.pet.domain.model.ExpenseCategory
import ru.finny.pet.domain.model.LessonDefinition
import ru.finny.pet.domain.model.LessonTheme
import ru.finny.pet.domain.model.SavingsGoal
import ru.finny.pet.domain.model.Season
import ru.finny.pet.domain.model.ShopItemDefinition
import ru.finny.pet.domain.model.ShopItemKind
import ru.finny.pet.domain.model.TaskDefinition
import ru.finny.pet.domain.model.TaskOption
import ru.finny.pet.domain.model.TaskType
import ru.finny.pet.domain.model.TestQuestion
import ru.finny.pet.domain.model.WorkDefinition
import ru.finny.pet.domain.model.WorkDifficulty

class ContentRepository(private val context: Context) {

    val works: List<WorkDefinition> by lazy { loadWorks() }
    val shopItems: List<ShopItemDefinition> by lazy { loadShop() }
    val goals: List<SavingsGoal> by lazy { loadGoals() }
    val clothing: List<ClothingItemDefinition> by lazy { loadClothing() }
    val tasks: List<TaskDefinition> by lazy { loadTasks() }
    val lessons: List<LessonDefinition> by lazy { loadLessons() }

    private fun readAsset(name: String): String =
        context.assets.open("content/$name").bufferedReader().use { it.readText() }

    private fun loadWorks(): List<WorkDefinition> {
        val root = JSONObject(readAsset("works.json"))
        val arr = root.getJSONArray("works")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val id = o.getString("id")
            WorkDefinition(
                id = id,
                title = o.getString("title"),
                description = o.getString("description"),
                difficulty = parseDifficulty(o.optString("difficulty", "EASY")),
                isPhysical = o.getBoolean("isPhysical"),
                imageAssetPath = o.optString("image").ifBlank { "$id.png" }
            )
        }
    }

    private fun parseDifficulty(raw: String): WorkDifficulty = when (raw.uppercase().trim()) {
        "HARD", "LONG" -> WorkDifficulty.HARD
        "MEDIUM" -> WorkDifficulty.MEDIUM
        "EASY", "FAST" -> WorkDifficulty.EASY
        else -> WorkDifficulty.EASY
    }

    private fun loadShop(): List<ShopItemDefinition> {
        val root = JSONObject(readAsset("shop_and_goals.json"))
        val arr = root.getJSONArray("shop")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            ShopItemDefinition(
                id = o.getString("id"),
                title = o.getString("title"),
                description = o.getString("description"),
                price = o.getInt("price"),
                category = ExpenseCategory.valueOf(o.getString("category")),
                kind = ShopItemKind.valueOf(o.getString("kind")),
                joyGain = o.optInt("joyGain", 0),
                hungerGain = o.optInt("hungerGain", 0)
            )
        }
    }

    private fun loadGoals(): List<SavingsGoal> {
        val root = JSONObject(readAsset("shop_and_goals.json"))
        val arr = root.getJSONArray("goals")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            SavingsGoal(
                id = o.getString("id"),
                title = o.getString("title"),
                targetAmount = o.getInt("targetAmount"),
                isActive = i == 0
            )
        }
    }

    private fun loadClothing(): List<ClothingItemDefinition> {
        val root = JSONObject(readAsset("clothing.json"))
        val arr = root.getJSONArray("clothing")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val variants = o.getJSONArray("variants")
            ClothingItemDefinition(
                id = o.getString("id"),
                season = Season.valueOf(o.getString("season")),
                title = o.getString("title"),
                basePrice = o.getInt("basePrice"),
                isMandatory = o.getBoolean("isMandatory"),
                variants = (0 until variants.length()).map { v ->
                    val vo = variants.getJSONObject(v)
                    ClothingVariantDefinition(
                        id = vo.getString("id"),
                        title = vo.getString("title"),
                        colorHex = vo.getString("colorHex"),
                        price = vo.getInt("price"),
                        isBase = vo.getBoolean("isBase")
                    )
                }
            )
        }
    }

    private fun loadTasks(): List<TaskDefinition> {
        val root = JSONObject(readAsset("tasks.json"))
        val arr = root.getJSONArray("tasks")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val options = o.getJSONArray("options")
            TaskDefinition(
                id = o.getString("id"),
                theme = LessonTheme.valueOf(o.getString("theme")),
                title = o.getString("title"),
                situation = o.getString("situation"),
                type = TaskType.valueOf(o.getString("type")),
                distributeTotal = if (o.has("distributeTotal")) o.getInt("distributeTotal") else null,
                options = parseOptions(options),
                correctExplanation = o.getString("correctExplanation"),
                wrongExplanation = o.getString("wrongExplanation")
            )
        }
    }

    private fun parseOptions(arr: JSONArray): List<TaskOption> =
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            TaskOption(
                id = o.getString("id"),
                text = o.getString("text"),
                isCorrect = o.getBoolean("isCorrect"),
                consequenceBalance = o.optInt("consequenceBalance", 0)
            )
        }

    private fun loadLessons(): List<LessonDefinition> {
        val root = JSONObject(readAsset("lessons.json"))
        val arr = root.getJSONArray("lessons")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val qs = o.getJSONArray("testQuestions")
            LessonDefinition(
                id = o.getString("id"),
                theme = LessonTheme.valueOf(o.getString("theme")),
                title = o.getString("title"),
                theoryText = o.getString("theoryText"),
                testQuestions = (0 until qs.length()).map { q ->
                    val qo = qs.getJSONObject(q)
                    val opts = qo.getJSONArray("options")
                    TestQuestion(
                        id = qo.getString("id"),
                        text = qo.getString("text"),
                        options = (0 until opts.length()).map { opts.getString(it) },
                        correctIndex = qo.getInt("correctIndex"),
                        miniActionHint = qo.getString("miniActionHint"),
                        explanation = qo.getString("explanation")
                    )
                }
            )
        }
    }

    fun clothingForSeason(season: Season): List<ClothingItemDefinition> =
        clothing.filter { it.season == season }

    fun workImage(fileName: String): ImageBitmap? {
        cacheKey(fileName)?.let { return it.asImageBitmap() }
        return try {
            context.assets.open("content/images/works/$fileName").use { input ->
                val bmp: Bitmap = BitmapFactory.decodeStream(input) ?: return null
                imageCache[fileName] = bmp
                bmp.asImageBitmap()
            }
        } catch (_: Exception) {
            null
        }
    }

    fun petImage(speciesFileName: String): ImageBitmap? {
        val key = "pet_$speciesFileName"
        cacheKey(key)?.let { return it.asImageBitmap() }
        return try {
            context.assets.open("content/images/pets/$speciesFileName.png").use { input ->
                val bmp: Bitmap = BitmapFactory.decodeStream(input) ?: return null
                imageCache[key] = bmp
                bmp.asImageBitmap()
            }
        } catch (_: Exception) {
            null
        }
    }

    fun animalImage(speciesFileName: String, sleeping: Boolean): ImageBitmap? {
        val fileName = "$speciesFileName${if (sleeping) "2" else "1"}.png"
        val key = "animal_$fileName"
        cacheKey(key)?.let { return it.asImageBitmap() }
        return try {
            context.assets.open("content/images/pets/$fileName").use { input ->
                val bmp: Bitmap = BitmapFactory.decodeStream(input) ?: return null
                imageCache[key] = bmp
                bmp.asImageBitmap()
            }
        } catch (_: Exception) {
            null
        }
    }

    fun backgroundImage(): ImageBitmap? {
        cacheKey("background")?.let { return it.asImageBitmap() }
        return try {
            val resId = context.resources.getIdentifier("background", "drawable", context.packageName)
            if (resId == 0) return null
            context.resources.openRawResource(resId).use { input ->
                val bmp: Bitmap = BitmapFactory.decodeStream(input) ?: return null
                imageCache["background"] = bmp
                bmp.asImageBitmap()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun cacheKey(name: String): Bitmap? = imageCache[name]?.takeIf { !it.isRecycled }

    private val imageCache = java.util.concurrent.ConcurrentHashMap<String, Bitmap>()
}
