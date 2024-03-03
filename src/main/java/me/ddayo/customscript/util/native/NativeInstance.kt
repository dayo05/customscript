package me.ddayo.customscript.util.native

abstract class NativeInstance<T> {
    private var isAllocated = false

    fun allocate() {
        if(isAllocated) return
        allocateInternal()
        isAllocated = true
    }

    fun free() {
        if(!isAllocated) return
        freeInternal()
        isAllocated = false
    }

    protected abstract fun allocateInternal()
    protected abstract fun freeInternal()

    abstract var nativeInstance: T
        protected set

    inline fun<R> claim(f: (instance: T) -> R): R {
        allocate()
        return f(nativeInstance)
    }
}