package com.serpenteando.cobra3d;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Teste que roda dentro do celular (instrumentado).
 * Serve pra confirmar que o pacote do app subiu certo no aparelho.
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Pega o contexto do app que está sendo testado
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.serpenteando.cobra3d", appContext.getPackageName());
    }
}