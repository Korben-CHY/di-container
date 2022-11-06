package com.tdd.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InjectionTest {
    private Dependency dependency = mock(Dependency.class);
    private Context context = mock(Context.class);

    @BeforeEach
    public void setup() {
        when(context.get(eq(Dependency.class))).thenReturn(Optional.of(dependency));
    }

    static class DefaultConstructor {
    }

    static class InjectConstructor {
        Dependency dependency;
        @Inject
        public InjectConstructor(Dependency dependency) {
            this.dependency = dependency;
        }
    }

    static class MultiInjectConstructors {
        @Inject
        public MultiInjectConstructors(AnotherDependency dependency) {
        }
        @Inject
        public MultiInjectConstructors(Dependency dependency) {
        }
    }

    static class NoInjectNorDefaultConstructor {
        public NoInjectNorDefaultConstructor(Dependency dependency) {
        }
    }

    static class ComponentWithFieldInjection {
        @Inject
        Dependency dependency;
    }
    static class SubclassWithFieldInjection extends ComponentWithFieldInjection {
    }

    static class FinalInjectField {
        @Inject
        final Dependency dependency = null;
    }

    static class InjectMethodWithNoDependency {
        boolean called = false;
        @Inject
        void install() {
            this.called = true;
        }
    }
    static class InjectMethodWithDependency {
        Dependency dependency;
        @Inject
        void install(Dependency dependency) {
            this.dependency = dependency;
        }
    }

    static class SuperClassWithInjectMethod {
        int superCalled = 0;
        @Inject
        void install() {
            superCalled++;
        }
    }

    static class SubclassWithInjectMethod extends SuperClassWithInjectMethod {
        int subCalled = 0;
        @Inject
        void installAnother() {
            subCalled = superCalled + 1;
        }
    }

    static class SubclassOverrideSuperClassWithInject extends SuperClassWithInjectMethod {
        @Inject
        void install() {
            super.install();
        }
    }

    static class SubclassOverrideSuperClassWithNoInject extends SuperClassWithInjectMethod {
        void install() {
            super.install();
        }
    }


    @Nested
    public class ConstructorInjection {

        @Nested
        class Injection {



            @Test
            public void should_call_default_constructor_if_no_inject_constructor() {
                DefaultConstructor instance = new ConstructorInjectionProvider<>(DefaultConstructor.class).get(context);
                assertNotNull(instance);
            }



            @Test
            public void should_inject_dependency_via_inject_constructor() {
                InjectConstructor instance = new ConstructorInjectionProvider<>(InjectConstructor.class).get(context);
                assertSame(dependency, instance.dependency);
            }

            @Test
            public void should_include_dependency_from_inject_constructor() {
                ConstructorInjectionProvider<InjectConstructor> provider = new ConstructorInjectionProvider<>(InjectConstructor.class);
                assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
            }
        }

        @Nested
        class IllegalInjectConstructors {
            abstract class AbstractComponent implements Component {
                @Inject
                public AbstractComponent() {
                }
            }
            @Test
            public void should_throw_exception_if_component_is_abstract() {
                assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(AbstractComponent.class));
            }
            @Test
            public void should_throw_exception_if_component_is_interface() {
                assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(Component.class));
            }

            @Test
            public void should_throw_exception_if_multi_inject_constructors_provided() {
                assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(MultiInjectConstructors.class));
            }

            @Test
            public void should_throw_exception_if_no_inject_nor_default_constructor_provided() {
                assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(NoInjectNorDefaultConstructor.class));
            }
        }
    }
    @Nested
    public class FieldInjection {
        @Nested
        class Injection {
            @Test
            public void should_inject_dependency_via_field() {
                ComponentWithFieldInjection component = new ConstructorInjectionProvider<>(ComponentWithFieldInjection.class).get(context);
                assertSame(dependency, component.dependency);
            }
            @Test
            public void should_inject_dependency_via_superclass_inject_field() {
                SubclassWithFieldInjection component = new ConstructorInjectionProvider<>(SubclassWithFieldInjection.class).get(context);
                assertSame(dependency, component.dependency);
            }
            @Test
            public void should_include_dependency_from_field_dependency() {
                ConstructorInjectionProvider<ComponentWithFieldInjection> provider = new ConstructorInjectionProvider<>(ComponentWithFieldInjection.class);
                assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
            }
        }

        @Nested
        class IllegalInjectFields {

            @Test
            public void should_throw_exception_if_inject_field_is_final() {
                assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(FinalInjectField.class));
            }
        }
    }

    static class InjectMethodWithTypeParameter {
        @Inject
        <T> void install() {
        }
    }
    @Nested
    public class MethodInjection {
        @Nested
        class Injection {

            @Test
            public void should_call_inject_method_even_if_no_dependency_declared() {
                InjectMethodWithNoDependency component = new ConstructorInjectionProvider<>(InjectMethodWithNoDependency.class).get(context);
                assertTrue(component.called);
            }

            @Test
            public void should_inject_dependency_via_inject_method() {
                InjectMethodWithDependency component = new ConstructorInjectionProvider<>(InjectMethodWithDependency.class).get(context);
                assertSame(dependency, component.dependency);
            }


            @Test
            public void should_inject_dependencies_via_inject_method_from_superclass() {
                SubclassWithInjectMethod component = new ConstructorInjectionProvider<>(SubclassWithInjectMethod.class).get(context);
                assertEquals(1, component.superCalled);
                assertEquals(2, component.subCalled);
            }

            @Test
            public void should_only_call_once_if_subclass_override_inject_method_with_inject() {
                SubclassOverrideSuperClassWithInject component = new ConstructorInjectionProvider<>(SubclassOverrideSuperClassWithInject.class).get(context);
                assertEquals(1, component.superCalled);
            }

            @Test
            public void should_not_call_inject_method_if_override_with_no_inject() {
                SubclassOverrideSuperClassWithNoInject component = new ConstructorInjectionProvider<>(SubclassOverrideSuperClassWithNoInject.class).get(context);
                assertEquals(0, component.superCalled);
            }
            @Test
            public void should_include_dependencies_from_inject_method() {
                ConstructorInjectionProvider<InjectMethodWithDependency> provider = new ConstructorInjectionProvider<>(InjectMethodWithDependency.class);
                assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
            }
        }
        @Nested
        class IllegalInjectMethods {

            @Test
            public void should_throw_exception_if_inject_method_has_type_parameter() {
                assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(InjectMethodWithTypeParameter.class));
            }
        }
    }
}