package celestialexpressions

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.*

val STANDARD_MODULE: Module = Module("std",
    VariableList(hashMapOf(
        "PI" to {Math.PI},
        "pi" to {Math.PI},
        "e" to {Math.E},
        "E" to {Math.E},
        "maxInteger" to {Int.MAX_VALUE.toDouble()},
        "minInteger" to {Int.MIN_VALUE.toDouble()},
        "maxDouble" to {Double.MAX_VALUE},
        "minDouble" to {Double.MIN_VALUE},
        "localDayOfYear" to {LocalDate.now().dayOfYear.toDouble()},
        "localDayOfMonth" to {LocalDate.now().dayOfMonth.toDouble()},
        "localDayOfWeek" to {LocalDate.now().dayOfWeek.value.toDouble()},
        "localMonth" to {LocalDate.now().month.value.toDouble()},
        "localYear" to {LocalDate.now().year.toDouble()},
        "localSecondOfHour" to {LocalDateTime.now().second.toDouble()},
        "localMinuteOfHour" to {LocalDateTime.now().minute.toDouble()},
        "localSecondOfDay" to {(((LocalDate.now().atTime(LocalTime.now()).getHour() * 60) + LocalDate.now().atTime(LocalTime.now()).getMinute() * 60) + LocalDate.now().atTime(LocalTime.now()).getSecond()).toDouble()},
        "localMinuteOfDay" to {((LocalDate.now().atTime(LocalTime.now()).getHour() * 60) + LocalDate.now().atTime(LocalTime.now()).getMinute()).toDouble()},
        "localHour" to {LocalTime.now().hour.toDouble()},
        "epochMilli" to { Instant.now().toEpochMilli().toDouble() },
        "random" to {Math.random()},
        )),
    FunctionList(
        "min" to Function({ arr -> minOf(arr[0].toDouble(), *arr.toDoubleCollection()) },-1),
        "max" to Function({ arr -> maxOf(arr[0].toDouble(), *arr.toDoubleCollection()) },-1),
        "sin" to Function({ arr -> sin(Math.toRadians(arr[0].toDouble()))}, 1),
        "cos" to Function({ arr -> cos(Math.toRadians(arr[0].toDouble()))}, 1),
        "tan" to Function({ arr -> tan(Math.toRadians(arr[0].toDouble())) }, 1),
        "sinr" to Function({ arr -> sin((arr[0].toDouble()))}, 1),
        "cosr" to Function({ arr -> cos((arr[0].toDouble()))}, 1),
        "tanr" to Function({ arr -> tan((arr[0].toDouble())) }, 1),
        "asin" to Function({ arr -> asin(Math.toRadians(arr[0].toDouble()))}, 1),
        "acos" to Function({ arr -> acos(Math.toRadians(arr[0].toDouble()))}, 1),
        "atan" to Function({ arr -> atan(Math.toRadians(arr[0].toDouble())) }, 1),
        "asinr" to Function({ arr -> asin((arr[0].toDouble()))}, 1),
        "acosr" to Function({ arr -> acos((arr[0].toDouble()))}, 1),
        "atanr" to Function({ arr -> atan((arr[0].toDouble())) }, 1),
        "radians" to Function({ arr -> Math.toRadians(arr[0].toDouble())}, 1),
        "deg" to Function({ arr -> Math.toDegrees(arr[0].toDouble())}, 1),
        "floor" to Function({ arr -> floor(arr[0].toDouble())}, 1),
        "ceil" to Function({ arr -> ceil(arr[0].toDouble()) }, 1),
        "round" to Function({ arr -> round(arr[0].toDouble()) }, 1),
        "abs" to Function({ arr -> abs(arr[0].toDouble())}, 1),
        "sqrt" to Function({ arr -> sqrt(arr[0].toDouble()) }, 1),
        "consolelog" to Function({ arr -> println(arr[0]); 0.0}, 1),
        "ifElse" to Function({arr -> if (arr[0].toDouble() != 0.0) { arr[1].toDouble() } else { arr[2].toDouble() } }, 3),
    )
)

fun Any.toDouble(): Double {
    return when (this) {
        is Number -> this.toDouble()
        is String -> this.toDoubleOrNull() ?: throw NumberFormatException("\"$this\" is not a valid number.")
        is Boolean -> if (this) 1.0 else 0.0
        else -> 0.0
    }
}

fun <T> Collection<T>.toDoubleCollection(): Array<Double> = this.map { it?.toDouble() ?: 0.0 }.toTypedArray()

data class ExpressionContext(val modules: ArrayList<Module> = ArrayList()) {
    init {
        modules.add(0, STANDARD_MODULE)
    }

    fun addModule(module: Module) = modules.add(module)
    fun hasVariable(name: String): Boolean {
        for (module in modules)
            if (module.hasVariable(name)) return true
        return false
    }

    fun getVariable(name: String): ()->Double {
        scanVariableConflicts(name)
        for (module in modules) {
            if (module.hasVariable(name)) return module.getVariable(name)
        }
        throw NoSuchVariableException("No variable named \"$name\" is declared.")
    }

    fun scanVariableConflicts(name: String) {
        val found = ArrayList<String>()
        for (module in modules) {
            if (module.hasVariable(name)) found.add(module.name)
        }
        if (found.size > 1) throw ConflictException(name, found)
    }
    fun hasFunction(name: String, argCount: Int): Boolean {
        for (module in modules)
            if (module.hasFunction(name, argCount)) return true
        return false
    }
    fun getFunction(name: String, argCount: Int): Function {
        scanFunctionConflicts(name, argCount)
        for (module in modules) {
            if (module.hasFunction(name, argCount)) return module.getFunction(name, argCount)
            if (module.hasFunction(name, -1)) return module.getFunction(name, -1)
        }
        /*this.modules.forEach {
            println(it)
        }*/
        throw NoSuchFunctionException("No function named \"$name\" with $argCount argument${if (argCount > 1) "s" else ""} is declared.")
    }
    fun scanFunctionConflicts(name: String, argCount: Int) {
        val found = ArrayList<String>()
        for (module in modules) {
            if (module.hasFunction(name, argCount)) found.add(module.name)
        }
        if (found.size > 1) throw ConflictException(name, found, "Function")
    }
}

class NoSuchVariableException(s: String): Exception(s)
class NoSuchFunctionException(s: String): Exception(s)
class ConflictException(variable: String, modules: ArrayList<String>, type:String = "Variable"):
    Exception("$type \"$variable\" found in multiple modules: ${modules.joinToString(", ") {it}}.")

open class Module(
    public val name: String,
    protected val variables: VariableList = VariableList(),
    protected val functions: FunctionList = FunctionList(),
) {

    open fun getVariable(name: String) = this.variables.getVariable(name.split(':').last())
    open fun hasVariable(name: String): Boolean {
        val split = name.split(':')
        if (split.size > 2) throw NoSuchVariableException("Illegal variable name \"$name\", cannot have more than one colon.")
        return if (split[0] == this.name || split.size == 1) this.variables.hasVariable(split.last())
        else false
    }

    open fun getFunction(name: String, argCount: Int) =
        this.functions.getFunction(name.split(':').last(), argCount) ?:
        throw AssemblyError("Function \"$name\" is not declared.")


    open fun hasFunction(name: String, argCount: Int): Boolean {
        val split = name.split(':')
        if (split.size > 2) throw NoSuchFunctionException("Illegal function name \"$name\", cannot have more than one colon.")
        return if (split[0] == this.name || split.size == 1) this.functions.hasFunction(split.last(), argCount)
        else false
    }

    override fun toString(): String {
        val out = StringBuilder()
        this.functions.functions.forEach {
            out.append("${it.key.first}, ${it.key.second}\n")
        }
        return out.toString()
    }
}
class ModuleBuilder(val name: String) {
    private val variables = VariableList()
    private val functions = FunctionList()

    fun addVariable(name: String, supplier: ()->Double) = variables.registerVariable(name, supplier)
    fun addFunction(name: String, function: Function) = functions.registerFunction(name, function)

    fun build() = Module(name, variables)
}