import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class UberBuildExtension {

    private final Map<String, Map<String, Object>> projectsConfig = new LinkedHashMap<>();
    private Runnable onInclude;
    private final AtomicBoolean configured = new AtomicBoolean();

    @Inject
    public abstract ObjectFactory getObjects();

    void onInclude(Runnable action) {
        onInclude = action;
    }

    Map<String, Map<String, Object>> getProjectsConfig() {
        return projectsConfig;
    }

    public void includes(Action<? super IncludesSpec> spec) {
        if (!configured.compareAndSet(false, true)) {
            throw new RuntimeException("The 'includes' method can only be called once, please merge your configuration within a single 'includes' block");
        }
        IncludesSpec specs = getObjects().newInstance(IncludesSpec.class);
        spec.execute(specs);
        for (IncludeSpec includeSpec : specs.getSpecs()) {
            HashMap<String, Object> config = new HashMap<>();
            config.put("branch", includeSpec.branch);
            config.put("githubSlug", includeSpec.slug);
            projectsConfig.put(includeSpec.name, config);
        }
        onInclude.run();
    }

    public static class IncludesSpec {
        private final List<IncludeSpec> specs = new ArrayList<IncludeSpec>();
        private final ObjectFactory objects;

        @Inject
        public IncludesSpec(ObjectFactory objects) {
            this.objects = objects;
        }

        public void micronaut(String name, Action<? super IncludeSpec> spec) {
            IncludeSpec include = objects.newInstance(IncludeSpec.class, name);
            spec.execute(include);
            this.specs.add(include);
        }

        public void micronaut(String name) {
            micronaut(name, s -> {});
        }
        public final List<IncludeSpec> getSpecs() {
            return specs;
        }

    }

    public static class IncludeSpec {
        @Inject
        public IncludeSpec(String name) {
            this.name = name;
        }

        public void branch(String branch) {
            this.branch = branch;
        }

        public void slug(String slug) {
            this.slug = slug;
        }

        private final String name;
        private String branch = "master";
        private String slug;
    }

}
