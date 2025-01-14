/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common

import org.jetbrains.kotlin.config.LoggingContext
import org.jetbrains.kotlin.backend.common.PreSerializationLoweringContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseEngine
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.fir.pipeline.Fir2IrActualizedResult
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.inline.PreSerializationLoweringPhasesProvider

fun PhaseEngine<PreSerializationLoweringContext>.runPreSerializationLoweringPhases(
    irModuleFragment: IrModuleFragment,
    loweringProvider: PreSerializationLoweringPhasesProvider<PreSerializationLoweringContext>,
    configuration: CompilerConfiguration,
): IrModuleFragment =
    runPhase(
        loweringProvider.lowerings(configuration),
        irModuleFragment,
        disable = !configuration.languageVersionSettings.supportsFeature(LanguageFeature.IrInlinerBeforeKlibSerialization),
    )

fun PhaseEngine<PreSerializationLoweringContext>.runPreSerializationLoweringPhases(
    fir2IrActualizedResult: Fir2IrActualizedResult,
    loweringProvider: PreSerializationLoweringPhasesProvider<PreSerializationLoweringContext>,
    configuration: CompilerConfiguration,
): Fir2IrActualizedResult = fir2IrActualizedResult.copy(
    irModuleFragment = runPreSerializationLoweringPhases(
        fir2IrActualizedResult.irModuleFragment,
        loweringProvider,
        configuration,
    )
)

@JvmName("runPreSerializationLoweringPhasesWithNewEngine")
fun <Context : LoggingContext> PhaseEngine<Context>.runPreSerializationLoweringPhases(
    fir2IrActualizedResult: Fir2IrActualizedResult,
    loweringProvider: PreSerializationLoweringPhasesProvider<PreSerializationLoweringContext>,
    configuration: CompilerConfiguration,
): Fir2IrActualizedResult = newEngine(PreSerializationLoweringContext(fir2IrActualizedResult.irBuiltIns, configuration)) { engine ->
    engine.runPreSerializationLoweringPhases(fir2IrActualizedResult, loweringProvider, configuration)
}
