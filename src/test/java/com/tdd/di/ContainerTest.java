package com.tdd.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

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
            assertSame(component, context.get(Component.class).get());
        }
        // TODO abstract class
        // TODO interface

        @Test
        public void should_return_empty_if_not_found() {
            Optional<Component> component = context.get(Component.class);
            assertTrue(component.isEmpty());
        }

        @Nested
        public class ConstructorInjection {
            @Test
            public void should_bind_type_to_a_class_with_default_constructor() {
                context.bind(Component.class, ComponentWithDefaultConstructor.class);

                Component component = context.get(Component.class).get();

                assertNotNull(component);
                assertTrue(component instanceof ComponentWithDefaultConstructor);
            }

            @Test
            public void should_bind_type_to_a_class_with_dependency_constructor() {
                Dependency dependency = new Dependency() {
                };

                context.bind(Component.class, ComponentWithDependencyConstructor.class);
                context.bind(Dependency.class, dependency);

                Component instance = context.get(Component.class).get();
                assertNotNull(instance);
                assertSame(dependency, ((ComponentWithDependencyConstructor) instance).getDependency());
            }

            @Test
            public void should_bind_type_to_a_class_with_transitive_dependency_constructor() {
                String dependencyStr = "inject dependency";
                context.bind(Component.class, ComponentWithDependencyConstructor.class);
                context.bind(Dependency.class, DependencyWithInjectConstructor.class);
                context.bind(String.class, dependencyStr);

                Component instance = context.get(Component.class).get();
                assertNotNull(instance);

                Dependency dependency = ((ComponentWithDependencyConstructor) instance).getDependency();
                assertNotNull(dependency);

                assertEquals(dependencyStr, ((DependencyWithInjectConstructor) dependency).getDependency());
            }

            @Test
            public void should_throw_exception_if_multi_inject_constructor_provided() {
                assertThrows(IllegalComponentException.class, () -> {
                    context.bind(Component.class, ComponentWithMultiInjectConstructor.class);
                });
            }

            @Test
            public void should_throw_exception_if_no_inject_constructor_nor_default_constructor_provided() {
                assertThrows(IllegalComponentException.class, () -> {
                    context.bind(Component.class, ComponentWithNoDefaultConstructorAndNoInjectConstructor.class);
                });
            }

            @Test
            public void should_throw_exception_if_no_dependency_found() {
                context.bind(Component.class, ComponentWithDependencyConstructor.class);

                DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> context.get(Component.class));

                assertEquals(Dependency.class, exception.getDependency());
            }

            @Test
            public void should_throw_exception_if_cyclic_dependency_found() {
                context.bind(Component.class, ComponentWithDependencyConstructor.class);
                context.bind(Dependency.class, DependencyWithComponentDependency.class);

                CyclicDependencyException exception = assertThrows(CyclicDependencyException.class, () -> context.get(Component.class));

                assertEquals(exception.getComponent().size(), 2);
                assertTrue(exception.getComponent().contains(Dependency.class));
                assertTrue(exception.getComponent().contains(Component.class));
            }

            @Test
            public void should_throw_exception_if_transitive_cyclic_dependency_found() {
                context.bind(Component.class, ComponentWithDependencyConstructor.class);
                context.bind(Dependency.class, DependencyDependOnAnotherDependency.class);
                context.bind(AnotherDependency.class, AnotherDependencyDependOnComponent.class);

                CyclicDependencyException exception = assertThrows(CyclicDependencyException.class, () -> context.get(Component.class));

                assertEquals(exception.getComponent().size(), 3);
                assertTrue(exception.getComponent().contains(Dependency.class));
                assertTrue(exception.getComponent().contains(Component.class));
                assertTrue(exception.getComponent().contains(AnotherDependency.class));

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

interface AnotherDependency {
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

class ComponentWithMultiInjectConstructor implements Component {
    @Inject
    public ComponentWithMultiInjectConstructor(String name) {
    }

    @Inject
    public ComponentWithMultiInjectConstructor(int name) {
    }
}

class ComponentWithNoDefaultConstructorAndNoInjectConstructor implements Component {

    public ComponentWithNoDefaultConstructorAndNoInjectConstructor(String name) {
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

class DependencyWithComponentDependency implements Dependency {
    private Component component;

    @Inject
    public DependencyWithComponentDependency(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }
}

class DependencyDependOnAnotherDependency implements Dependency {
    private AnotherDependency anotherDependency;

    @Inject
    public DependencyDependOnAnotherDependency(AnotherDependency anotherDependency) {
        this.anotherDependency = anotherDependency;
    }
}

class AnotherDependencyDependOnComponent implements AnotherDependency {
    private Component component;

    @Inject
    public AnotherDependencyDependOnComponent(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }
}
