interface HasPrivateClassFieldGetter {
    /**
     * Gets a named private field of a given class instance.
     */
    fun <T : Any, V> getPrivateClassField(classInstance: T, fieldName: String): V {
        val field = (classInstance).javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return field.get(classInstance) as V
    }
}