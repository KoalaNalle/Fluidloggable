package com.moigferdsrte.fluidloggable.gametest;

import com.moigferdsrte.fluidloggable.block.*;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

final class NeoForgeGameTests {
    private record Test(Identifier id, Object instance, Method method, int maxTicks) {}
    private static final List<Test> TESTS = new ArrayList<>();

    static void register(final IEventBus bus) {
        final DeferredRegister<Consumer<GameTestHelper>> functions =
                DeferredRegister.create(Registries.TEST_FUNCTION, "fluidloggable_gametest");
        for (Class<?> type : List.of(BlockDefaultStateGameTest.class, FullBlockEligibilityGameTest.class,
                BucketPlacementGameTest.class, DoorFlowGameTest.class, FlowingFluidTickGameTest.class,
                StoredFluidRandomTickGameTest.class, LavaloggedFlowGameTest.class,
                TrapdoorFlowGameTest.class, TaggedFluidloggableBlockGameTest.class)) {
            try {
                final Object instance = type.getConstructor().newInstance();
                for (Method method : type.getDeclaredMethods()) {
                    final var annotation = method.getAnnotation(GameTest.class);
                    if (annotation == null) continue;
                    final String name = (type.getSimpleName() + "_" + method.getName()).toLowerCase(Locale.ROOT);
                    final var test = new Test(Identifier.fromNamespaceAndPath("fluidloggable_gametest", name),
                            instance, method, annotation.maxTicks());
                    TESTS.add(test);
                    functions.register(name, () -> helper -> invoke(test, helper));
                }
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Cannot register gameplay tests", exception);
            }
        }
        functions.register(bus);
        bus.addListener(NeoForgeGameTests::registerTests);
    }

    private static void registerTests(final RegisterGameTestsEvent event) {
        final var environment = event.registerEnvironment(Identifier.fromNamespaceAndPath("fluidloggable_gametest", "default"));
        for (Test test : TESTS) {
            event.registerTest(test.id(), new FunctionGameTestInstance(
                    ResourceKey.create(Registries.TEST_FUNCTION, test.id()),
                    new TestData<>(environment, Identifier.fromNamespaceAndPath("fluidloggable_gametest", "empty"),
                            test.maxTicks(), 1, true)));
        }
    }

    private static void invoke(final Test test, final GameTestHelper helper) {
        try {
            test.method().invoke(test.instance(), helper);
        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof RuntimeException runtime) throw runtime;
            if (exception.getCause() instanceof Error error) throw error;
            throw new IllegalStateException(exception.getCause());
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
