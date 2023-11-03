architectury {
    common("forge", "fabric")
    platformSetupLoomIde()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    modCompileOnly("com.cobblemon:mod:${project.properties["cobblemon_version"]}")

    modApi("me.shedaniel.cloth:cloth-config:11.1.106")
}