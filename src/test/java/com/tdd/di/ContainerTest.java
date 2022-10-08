package com.tdd.di;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContainerTest {

    interface Component {
    }

    static public class ComponentWithDefaultConstructor implements Component {
        public ComponentWithDefaultConstructor() {
        }
    }

    @Nested
    public class ComponentConstruction {
        // TODO instance
        @Test
        public void should_bind_type_to_specified_instance() {
            Context context = new Context();
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
                Context context = new Context();

                context.bind(Component.class, ComponentWithDefaultConstructor.class);
                Component component = context.get(Component.class);
                assertNotNull(component);
                assertTrue(component instanceof ComponentWithDefaultConstructor);
            }

            // TODO with dependencies
            // TODO A -> B -> C
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
