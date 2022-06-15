# AutoGradlePluginProperties
Use to generate a gradle plugin properties file on META-INF/gradle-plugins/xxxx.properties

Use annotation @AutoPlugin to flag where need to create
@AutoPlugin has two params 
@param id ; gradle plugin id
@param value ;

Example:

1: generate com.team.android.plugin.properties

@AutoPlugin(id = "com.team.android.plugin", value = Plugin.class)
class beanPlugin implements Plugin<Project> {

    public beanPlugin(ToolingModelBuilderRegistry registry) {

        registry.register(new buildmode())
    }

    void apply(Project project) {
        project.tasks.create('myhello', GreetingTask)
    }
}

Or

2: generate $package.beanPlugin.properties
@AutoPlugin(Plugin.class)
class beanPlugin implements Plugin<Project> {

    public beanPlugin(ToolingModelBuilderRegistry registry) {

        registry.register(new buildmode())
    }
    void apply(Project project) {
        project.tasks.create('myhello', GreetingTask)
    }
}
