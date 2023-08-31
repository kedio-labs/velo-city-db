import java.io.FileNotFoundException

interface HasResourcePathGetter {
    companion object {
        fun getResourcePath(path: String): String =
            this::class.java.declaringClass.getResource(path)?.path
                ?: throw FileNotFoundException("Resource file not found $path")
    }
}