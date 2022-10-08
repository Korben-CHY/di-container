package com.tdd.di;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ContainerTest {

    interface Component {
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
            Assertions.assertSame(component, context.get(Component.class));
        }
        // TODO abstract class
        // TODO interface

        @Nested
        public class ConstructorInjection {
            // TODO No arguments constructor
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
