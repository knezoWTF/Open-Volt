oop is very important

fun main() {
    val vehicle1: Vehicle = Vehicle() // Here, an object of type 'Vehicle' is created and assigned to the variable 'vehicle1'.

    println(vehicle1.make)       // Printing the 'make' attribute of the 'vehicle1' object to the console. Output: Undefined
    println(vehicle1.model)      // Printing the 'model' attribute of the 'vehicle1' object to the console. Output: Undefined
    println(vehicle1.color)      // Printing the 'color' attribute of the 'vehicle1' object to the console. Output: Undefined
    println(vehicle1.engineSize) // Printing the 'engineSize' attribute of the 'vehicle1' object to the console. Output: 0.0
    vehicle1.driveForwards()     // Invocation of the 'driveForwards' method. Output: Undefined drives forward
    vehicle1.driveBackwards()    // Invocation of the 'driveBackwards' method. Output: Undefined drives backwards
    vehicle1.stop()              // Invocation of the 'stop' method. Output: Undefined stops
}

class Vehicle{
    // Definition of properties/attributes using variables
    var make: String = "Undefined"
    var model: String = "Undefined"
    var color: String = "Undefined"
    var engineSize: Float = 0.00f

    // Definition of methods using functions
    fun driveForwards() {
        println("$make drives forward")
    }

    fun driveBackwards() {
        println("$make drives backwards")
    }

    fun stop() {
        println("$make stops")
    }
}