package com.moigferdsrte.fluidloggable.gametest;

import com.moigferdsrte.fluidloggable.block.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/** Test metadata consumed by the NeoForge registration adapter. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GameTest {
    int maxTicks() default 100;
}
