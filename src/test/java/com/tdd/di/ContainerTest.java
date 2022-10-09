package com.tdd.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContainerTest {

    Context context;

    @BeforeEach
    public void setup() {
        context = new Context();
    }

    @Nested
    public class ComponentConstruction {
        // TODO instance
        @Test
        public void should_bind_type_to_specified_instance() {
            Component component = new Component() {
            };

            context.bind(Component.class, component);
            assertSame(component, context.get(Component.class));
        }
        // TODO abstract class
        // TODO interface

        @Nested
        public class ConstructorInjection {
            // TODO No arguments constructor
            @Test
            public void should_bind_type_to_a_class_with_default_constructor() {
                context.bind(Component.class, ComponentWithDefaultConstructor.class);

                Component component = context.get(Component.class);

                assertNotNull(component);
                assertTrue(component instanceof ComponentWithDefaultConstructor);
            }

            // TODO with dependencies
            @Test
            public void should_bind_type_to_a_class_with_dependency_constructor() {
                Dependency dependency = new Dependency() {
                };

                context.bind(Component.class, ComponentWithDependencyConstructor.class);
                context.bind(Dependency.class, dependency);

                Component instance = context.get(Component.class);
                assertNotNull(instance);
                assertSame(dependency, ((ComponentWithDependencyConstructor) instance).getDependency());
            }

            @Test
            public void should_bind_type_to_a_class_with_transitive_dependency_constructor() {
                String dependencyStr = "inject dependency";
                context.bind(Component.class, ComponentWithDependencyConstructor.class);
                context.bind(Dependency.class, DependencyWithInjectConstructor.class);
                context.bind(String.class, dependencyStr);

                Component instance = context.get(Component.class);
                assertNotNull(instance);

                Dependency dependency = ((ComponentWithDependencyConstructor) instance).getDependency();
                assertNotNull(dependency);

                assertEquals(dependencyStr, ((DependencyWithInjectConstructor) dependency).getDependency());
            }
        }

        @Nested
        public class FieldInjection {

        }

        @Nested
        public class MethodInjection {

        }
    }

    @Nested
    public class DependenciesSelection {

    }

    @Nested
    public class LifecycleManagement {

    }


}

interface Component {
}

interface Dependency {
}

class ComponentWithDefaultConstructor implements Component {
    public ComponentWithDefaultConstructor() {
    }
}

class ComponentWithDependencyConstructor implements Component {
    private Dependency dependency;

    @Inject
    public ComponentWithDependencyConstructor(Dependency dependency) {
        this.dependency = dependency;
    }

    public Dependency getDependency() {
        return dependency;
    }
}

class DependencyWithInjectConstructor implements Dependency {
    private String dependency;

    @Inject
    public DependencyWithInjectConstructor(String dependency) {
        this.dependency = dependency;
    }

    public String getDependency() {
        return dependency;
    }
}
