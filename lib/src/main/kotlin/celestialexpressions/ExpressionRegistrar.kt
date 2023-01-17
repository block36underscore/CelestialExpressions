package celestialexpressions

lateinit var registrar: ExpressionRegistrar

class ExpressionRegistrar {
    var modules: HashMap<String, Module> = HashMap()
    init {
        registrar = this
        this.modules.put("std", STANDARD_MODULE)
    }

    fun registerModule(name: String, module: Module) {
        if (name == "std" || name == "local") throw RegistrationError("Name of module can not be $name, as that name is reserved internally")
        if (modules.containsKey(name)) throw RegistrationError("celestialexpressions.Module named $name is already registered")
        this.modules.putIfAbsent(name, module)
    }

    fun getModule(name: String): Module {
        return if (modules.containsKey(name)) modules[name]!!
        else throw MissingModuleError(name)
    }

    fun getModules(names: Collection<String>): ArrayList<Module> {
        val list = ArrayList<Module>()
        for (name in names) list.add(this.getModule(name))
        return list
    }
}

class RegistrationError(s: String): Exception(s)
class MissingModuleError(name: String): Exception("celestialexpressions.Module $name not registered")