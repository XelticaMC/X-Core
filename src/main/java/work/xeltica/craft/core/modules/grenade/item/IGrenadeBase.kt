package work.xeltica.craft.core.modules.grenade.item

interface IGrenadeBase {
    val name: String

    fun throwGrenade()
    fun explode()
    fun kill()
}