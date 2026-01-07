package com.example.carpartsecom.util

/**
 * Virtual Car Assistant - Provides product recommendations and basic diagnostics
 */
object CarAssistant {

    data class AssistantResponse(
        val message: String,
        val productRecommendations: List<String> = emptyList(),
        val shouldConsultMechanic: Boolean = false
    )

    // Keywords for different car issues
    private val brakeKeywords = listOf("brake", "brakes", "braking", "stop", "stopping", "squeak", "squeaking", "grinding")
    private val engineKeywords = listOf("engine", "motor", "power", "acceleration", "accelerate", "slow")
    private val oilKeywords = listOf("oil", "lubricant", "lubrication", "oil change")
    private val batteryKeywords = listOf("battery", "start", "starting", "dead", "won't start", "electrical", "lights dim")
    private val sparkPlugKeywords = listOf("spark", "plug", "ignition", "misfire", "rough idle", "fuel economy")
    private val filterKeywords = listOf("filter", "air filter", "oil filter", "cabin filter", "dirty")
    private val noiseKeywords = listOf("noise", "sound", "rattle", "rattling", "clunk", "knock", "knocking", "humming", "whining")
    private val lightKeywords = listOf("light", "check engine", "warning", "dashboard", "indicator")
    private val tireKeywords = listOf("tire", "tyre", "wheel", "flat", "pressure", "worn")

    // Car brands/models for context
    private val carBrands = listOf("toyota", "honda", "ford", "chevrolet", "bmw", "mercedes", "audi", "volkswagen", "nissan", "hyundai", "kia", "mazda", "subaru", "jeep", "dodge", "ram", "gmc", "cadillac", "lexus", "acura", "infiniti", "porsche", "tesla", "volvo", "jaguar", "land rover", "mini", "fiat", "alfa romeo", "chrysler", "buick", "lincoln", "genesis", "mitsubishi", "suzuki", "peugeot", "renault", "citroen", "skoda", "seat")

    fun getResponse(userMessage: String): AssistantResponse {
        val message = userMessage.lowercase().trim()

        // Check for greetings
        if (isGreeting(message)) {
            return getGreetingResponse()
        }

        // Check for car info request
        if (message.contains("help") || message.contains("what can you do") || message.contains("?") && message.length < 20) {
            return getHelpResponse()
        }

        // Analyze the message for issues
        return when {
            containsAny(message, brakeKeywords) -> getBrakeResponse(message)
            containsAny(message, noiseKeywords) -> getNoiseResponse(message)
            containsAny(message, lightKeywords) -> getWarningLightResponse(message)
            containsAny(message, batteryKeywords) -> getBatteryResponse(message)
            containsAny(message, oilKeywords) -> getOilResponse(message)
            containsAny(message, sparkPlugKeywords) -> getSparkPlugResponse(message)
            containsAny(message, filterKeywords) -> getFilterResponse(message)
            containsAny(message, engineKeywords) -> getEngineResponse(message)
            containsAny(message, tireKeywords) -> getTireResponse(message)
            detectsCarInfo(message) -> getCarInfoResponse(message)
            else -> getDefaultResponse()
        }
    }

    private fun isGreeting(message: String): Boolean {
        val greetings = listOf("hi", "hello", "hey", "good morning", "good afternoon", "good evening", "howdy", "greetings")
        return greetings.any { message.startsWith(it) || message == it }
    }

    private fun containsAny(message: String, keywords: List<String>): Boolean {
        return keywords.any { message.contains(it) }
    }

    private fun detectsCarInfo(message: String): Boolean {
        return carBrands.any { message.contains(it) } ||
               message.contains("my car") ||
               message.contains("i have a") ||
               message.contains("i drive")
    }

    private fun getGreetingResponse(): AssistantResponse {
        return AssistantResponse(
            message = """
                👋 Hello! I'm your Virtual Car Assistant!
                
                I can help you with:
                🔧 Finding the right parts for your car
                🔍 Basic diagnostics for common issues
                💡 Maintenance recommendations
                
                Just tell me about your car or describe any issues you're experiencing!
                
                For example:
                • "I have a 2019 Toyota Camry and need new brakes"
                • "My engine light is on"
                • "I hear a squeaking noise when braking"
            """.trimIndent()
        )
    }

    private fun getHelpResponse(): AssistantResponse {
        return AssistantResponse(
            message = """
                🚗 Here's how I can help:
                
                **Product Recommendations:**
                Tell me your car (make, model, year) and what you need:
                • "I need brake pads for my Honda Civic"
                • "Looking for an oil filter for 2020 Ford F-150"
                
                **Diagnostics Help:**
                Describe your issue:
                • "My car won't start"
                • "I hear a grinding noise"
                • "Check engine light is on"
                
                **Maintenance Tips:**
                Ask about routine maintenance:
                • "When should I change my oil?"
                • "How often to replace spark plugs?"
                
                What would you like help with today?
            """.trimIndent()
        )
    }

    private fun getBrakeResponse(message: String): AssistantResponse {
        val hasSqueaking = message.contains("squeak") || message.contains("squeal")
        val hasGrinding = message.contains("grind")
        val needsNew = message.contains("need") || message.contains("replace") || message.contains("new") || message.contains("change")

        val diagnosticMessage = when {
            hasGrinding -> """
                ⚠️ **Grinding brakes are serious!**
                
                This usually means your brake pads are completely worn and metal is grinding on metal. This can damage your rotors.
                
                **Recommended Action:**
                🔴 Stop driving and have your brakes inspected immediately
                🔴 You likely need new brake pads AND possibly rotors
                
                **Our Recommendation:**
                I suggest our high-quality **Brake Pads** - they're designed for all weather conditions and provide excellent stopping power.
            """.trimIndent()

            hasSqueaking -> """
                🔔 **Squeaking brakes diagnosis:**
                
                This is often a warning indicator that your brake pads are wearing thin. Most brake pads have a metal indicator that squeaks when pads are low.
                
                **Possible Causes:**
                • Worn brake pads (most common)
                • Dust or debris on brakes
                • Moisture after rain/car wash
                
                **Recommended Action:**
                Have your brake pads inspected. If they're below 3mm, it's time to replace them.
            """.trimIndent()

            needsNew -> """
                🛠️ **Brake Replacement Help**
                
                Great that you're being proactive about brake maintenance!
                
                **What you typically need:**
                • Brake pads (replace every 30,000-70,000 miles)
                • Brake rotors (if warped or worn)
                • Brake fluid (check level and condition)
                
                **Our Products:**
                We have high-quality **Brake Pads** perfect for your needs!
            """.trimIndent()

            else -> """
                🔧 **Brake System Help**
                
                Brakes are crucial for your safety. Here's what to know:
                
                **Warning Signs:**
                • Squeaking or squealing
                • Grinding sounds
                • Car pulls to one side
                • Vibration when braking
                • Soft or spongy brake pedal
                
                **Maintenance Schedule:**
                • Inspect brakes every 12,000 miles
                • Replace pads every 30,000-70,000 miles
                
                Would you like me to recommend brake pads?
            """.trimIndent()
        }

        return AssistantResponse(
            message = diagnosticMessage,
            productRecommendations = listOf("Brake Pads"),
            shouldConsultMechanic = hasGrinding
        )
    }

    private fun getNoiseResponse(message: String): AssistantResponse {
        val isFromEngine = message.contains("engine") || message.contains("hood") || message.contains("motor")
        val isFromWheels = message.contains("wheel") || message.contains("tire") || message.contains("driving")
        val isFromBrakes = message.contains("brake") || message.contains("stop")

        val responseMessage = """
            🔊 **Diagnosing Car Noises**
            
            Car noises can indicate various issues. Let me help narrow it down:
            
            **Where is the noise coming from?**
            
            🔹 **From the engine area:**
            • Knocking = Possible engine/oil issue
            • Squealing = Belt problem
            • Hissing = Possible leak
            
            🔹 **When braking:**
            • Squeaking = Worn brake pads
            • Grinding = Severely worn pads (urgent!)
            
            🔹 **While driving:**
            • Humming = Wheel bearing or tires
            • Clunking = Suspension issue
            • Rattling = Loose parts or exhaust
            
            ⚠️ **Important:** Unusual noises often indicate something needs attention. If the noise is loud or persistent, please consult a mechanic for a proper diagnosis.
            
            Can you describe more specifically when the noise occurs?
        """.trimIndent()

        return AssistantResponse(
            message = responseMessage,
            shouldConsultMechanic = true
        )
    }

    private fun getWarningLightResponse(message: String): AssistantResponse {
        val isCheckEngine = message.contains("check engine") || message.contains("engine light")

        val responseMessage = if (isCheckEngine) {
            """
                🚨 **Check Engine Light On**
                
                The check engine light can indicate many things, from minor to serious:
                
                **Common Causes:**
                • Loose gas cap (try tightening it!)
                • Oxygen sensor failure
                • Catalytic converter issue
                • Mass airflow sensor
                • Spark plug/ignition coil problems
                
                **What to do:**
                1. If the light is steady (not flashing), it's usually not an emergency, but get it checked soon
                2. If the light is **flashing**, reduce speed and get to a mechanic immediately - this indicates a serious misfire
                
                **Quick Check:**
                • Is your gas cap tight?
                • Any unusual sounds or smells?
                • Is the car running rough?
                
                ⚠️ **Recommendation:** Have a mechanic read the diagnostic codes. Many auto parts stores offer free code reading.
                
                In the meantime, **Spark Plugs** are a common cause and easy to replace!
            """.trimIndent()
        } else {
            """
                💡 **Dashboard Warning Lights**
                
                Different lights mean different things:
                
                🔴 **Red lights** = Stop driving, serious issue
                🟡 **Yellow/Orange lights** = Caution, service soon
                🟢 **Green/Blue lights** = Information only
                
                **Common Warning Lights:**
                • 🔋 Battery light = Charging system issue
                • 🌡️ Temperature light = Engine overheating
                • 🛢️ Oil light = Low oil pressure (stop!)
                • ⚠️ Check engine = Various issues
                
                Which warning light are you seeing?
                
                ⚠️ **Important:** If you see a red warning light, please stop driving safely and consult a mechanic.
            """.trimIndent()
        }

        return AssistantResponse(
            message = responseMessage,
            productRecommendations = if (isCheckEngine) listOf("Spark Plug") else emptyList(),
            shouldConsultMechanic = true
        )
    }

    private fun getBatteryResponse(message: String): AssistantResponse {
        val wontStart = message.contains("won't start") || message.contains("wont start") || message.contains("not start") || message.contains("dead")

        val responseMessage = if (wontStart) {
            """
                🔋 **Car Won't Start - Battery Diagnosis**
                
                **Quick Test:**
                When you turn the key, what happens?
                
                • **Nothing at all** = Likely dead battery or connection issue
                • **Clicking sound** = Weak battery or starter issue
                • **Engine cranks but won't start** = Fuel or spark issue, not battery
                
                **Try This:**
                1. Check battery terminals for corrosion (white/green buildup)
                2. Make sure connections are tight
                3. Try a jump start
                
                **If jump start works:**
                Your battery may be old or failing. Batteries typically last 3-5 years.
                
                **Our Recommendation:**
                Check out our **Car Battery** - long-lasting and reliable!
                
                ⚠️ If jump starting doesn't help, the issue may be the starter or alternator - please see a mechanic.
            """.trimIndent()
        } else {
            """
                🔋 **Battery & Electrical System**
                
                **Signs of a Weak Battery:**
                • Slow engine crank
                • Dim headlights
                • Electrical issues
                • Battery warning light
                • Old battery (3+ years)
                
                **Battery Maintenance Tips:**
                • Keep terminals clean
                • Ensure tight connections
                • Test battery annually
                • Replace every 3-5 years
                
                **Our Product:**
                We have a high-quality **Car Battery** in stock - long-lasting and reliable for all conditions!
            """.trimIndent()
        }

        return AssistantResponse(
            message = responseMessage,
            productRecommendations = listOf("Car Battery"),
            shouldConsultMechanic = wontStart
        )
    }

    private fun getOilResponse(message: String): AssistantResponse {
        return AssistantResponse(
            message = """
                🛢️ **Oil & Lubrication**
                
                **Oil Change Schedule:**
                • Conventional oil: Every 3,000-5,000 miles
                • Synthetic oil: Every 7,500-10,000 miles
                • Check your owner's manual for specific recommendations
                
                **Signs You Need an Oil Change:**
                • Dark, dirty oil on dipstick
                • Oil change light on
                • Engine running louder than usual
                • Oil smell inside car
                
                **What You'll Need:**
                • Correct oil type and amount
                • **Oil Filter** (always change with oil!)
                • Drain plug washer
                
                **Our Product:**
                We have premium **Oil Filters** that provide excellent engine protection!
                
                💡 **Tip:** Never skip oil changes - it's the most important maintenance for engine longevity!
            """.trimIndent(),
            productRecommendations = listOf("Oil Filter")
        )
    }

    private fun getSparkPlugResponse(message: String): AssistantResponse {
        return AssistantResponse(
            message = """
                ⚡ **Spark Plugs & Ignition**
                
                **Signs of Bad Spark Plugs:**
                • Rough idle
                • Poor fuel economy
                • Engine misfires
                • Trouble starting
                • Lack of acceleration
                • Check engine light
                
                **Replacement Schedule:**
                • Copper plugs: 20,000-30,000 miles
                • Platinum plugs: 60,000 miles
                • Iridium plugs: 100,000 miles
                
                **Our Product:**
                We have **Iridium Spark Plugs** - they last longer and provide better ignition!
                
                💡 **Tip:** When replacing spark plugs, it's good to also check/replace ignition coils if your car has high mileage.
            """.trimIndent(),
            productRecommendations = listOf("Spark Plug")
        )
    }

    private fun getFilterResponse(message: String): AssistantResponse {
        return AssistantResponse(
            message = """
                🔄 **Filters Maintenance**
                
                Your car has several important filters:
                
                **Oil Filter:**
                • Change with every oil change
                • Keeps engine oil clean
                
                **Air Filter:**
                • Replace every 15,000-30,000 miles
                • Affects fuel economy and performance
                
                **Cabin Air Filter:**
                • Replace every 15,000-25,000 miles
                • Keeps interior air clean
                
                **Fuel Filter:**
                • Replace every 20,000-40,000 miles
                • Keeps fuel system clean
                
                **Our Product:**
                We have premium **Oil Filters** for excellent engine protection!
                
                💡 **Tip:** A clean air filter can improve fuel economy by up to 10%!
            """.trimIndent(),
            productRecommendations = listOf("Oil Filter")
        )
    }

    private fun getEngineResponse(message: String): AssistantResponse {
        return AssistantResponse(
            message = """
                🔧 **Engine Issues**
                
                Engine problems can range from simple fixes to complex repairs.
                
                **Common Engine Issues:**
                
                🔹 **Loss of Power:**
                • Clogged air filter
                • Worn spark plugs
                • Fuel system issues
                
                🔹 **Rough Running:**
                • Bad spark plugs
                • Vacuum leak
                • Fuel injector problems
                
                🔹 **Overheating:**
                • Low coolant
                • Failed thermostat
                • Water pump issue
                
                **Quick Maintenance Items:**
                • Check oil level regularly
                • Replace air filter
                • Change spark plugs on schedule
                
                ⚠️ **For serious engine issues**, please consult a professional mechanic for proper diagnosis.
                
                Would you like recommendations for spark plugs or filters?
            """.trimIndent(),
            productRecommendations = listOf("Spark Plug", "Oil Filter"),
            shouldConsultMechanic = true
        )
    }

    private fun getTireResponse(message: String): AssistantResponse {
        return AssistantResponse(
            message = """
                🛞 **Tire Information**
                
                **Tire Maintenance Tips:**
                • Check pressure monthly (including spare)
                • Rotate tires every 5,000-7,500 miles
                • Check tread depth regularly
                • Look for uneven wear patterns
                
                **Signs You Need New Tires:**
                • Tread depth below 2/32"
                • Visible wear bars
                • Cracks or bulges in sidewall
                • Vibration while driving
                
                **The Penny Test:**
                Insert a penny with Lincoln's head down. If you can see all of his head, it's time for new tires!
                
                ⚠️ We currently focus on mechanical parts, but tire shops can help with tire needs.
                
                Is there something else I can help you with?
            """.trimIndent(),
            shouldConsultMechanic = false
        )
    }

    private fun getCarInfoResponse(message: String): AssistantResponse {
        // Extract car brand if mentioned
        val mentionedBrand = carBrands.find { message.contains(it) }?.replaceFirstChar { it.uppercase() } ?: "your car"

        return AssistantResponse(
            message = """
                🚗 **Great, you have a $mentionedBrand!**
                
                How can I help you today?
                
                **Tell me:**
                • What part do you need? (brakes, battery, filters, etc.)
                • Or describe any issues you're experiencing
                
                **Popular Parts:**
                • 🔹 Brake Pads
                • 🔹 Oil Filter
                • 🔹 Spark Plugs
                • 🔹 Car Battery
                
                Just let me know what you need!
            """.trimIndent()
        )
    }

    private fun getDefaultResponse(): AssistantResponse {
        return AssistantResponse(
            message = """
                🤔 I'm not sure I understood that completely.
                
                **Try asking me about:**
                
                🔧 **Parts:** "I need brake pads for my car"
                🔍 **Issues:** "My engine light is on"
                🔊 **Noises:** "I hear a squeaking when braking"
                🔋 **Starting problems:** "My car won't start"
                
                **Or tell me:**
                • Your car make and model
                • What symptoms you're experiencing
                
                I'm here to help! 🚗
            """.trimIndent()
        )
    }
}

