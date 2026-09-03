/*
 * Copyright (c) 2026 David Aguiar Gonzalez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gc.david.dfm.main.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import gc.david.dfm.elevation.presentation.model.ElevationUiModel
import gc.david.dfm.elevation.presentation.view.ElevationChartView

/**
 * Elevation chart composable wrapping the custom ElevationChartView.
 * Uses AndroidView for interop with the existing View-based chart.
 */
@Composable
fun ElevationChart(
    isVisible: Boolean,
    elevationData: ElevationUiModel?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(expandFrom = Alignment.Top),
        exit = shrinkVertically(shrinkTowards = Alignment.Top),
        modifier = modifier
    ) {
        AndroidView(
            factory = { context ->
                ElevationChartView(context).apply {
                    setOnCloseListener { onClose() }
                }
            },
            update = { view ->
                elevationData?.let { data ->
                    view.setElevationProfile(data.elevationList)
                    view.setTitle(data.altitudeUnit)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(2.dp)
        )
    }
}


