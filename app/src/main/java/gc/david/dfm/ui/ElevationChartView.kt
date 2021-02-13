/*
 * Copyright (c) 2021 David Aguiar Gonzalez
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

package gc.david.dfm.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import gc.david.dfm.R

class ElevationChartView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    private val graphView: GraphView
    private val ivCloseChart: ImageView

    private var onCloseListener: (() -> Unit)? = null

    init {
        View.inflate(context, R.layout.view_elevation_chart, this)
        if (!isInEditMode) {
            layoutParams = LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(ContextCompat.getColor(context, R.color.elevation_chart_background))
        }

        ivCloseChart = findViewById(R.id.ivCloseChart)
        ivCloseChart.setOnClickListener { onCloseListener?.invoke() }

        graphView = findViewById(R.id.graphView)
        with(graphView) {
            titleTextSize = resources.getDimension(R.dimen.elevation_chart_text_size)
            titleColor = ContextCompat.getColor(context, R.color.white)
        }
    }

    fun setOnCloseListener(onCloseListener: (() -> Unit)) {
        this.onCloseListener = onCloseListener
    }

    fun setElevationProfile(elevationList: List<Double>) {
        with(graphView) {
            removeAllSeries()
            val series =
                    elevationList
                            .mapIndexed { index, _ ->
                                DataPoint(index.toDouble(), elevationList[index])
                            }
                            .toTypedArray()
            addSeries(LineGraphSeries(series).apply {
                color = ContextCompat.getColor(context, R.color.elevation_chart_line)
            })

            // For some reason, this code needs to be done after adding the series and not after init :s
            viewport.setScalableY(true)

            with(gridLabelRenderer) {
                numHorizontalLabels = 1

                isHorizontalLabelsVisible = false
                gridColor = ContextCompat.getColor(context, R.color.white)
                verticalAxisTitleColor = ContextCompat.getColor(context, R.color.white)
                verticalLabelsColor = ContextCompat.getColor(context, R.color.white)
            }
        }
    }

    fun setTitle(altitude: String) {
        graphView.title = resources.getString(R.string.elevation_chart_title, altitude)
    }
}

