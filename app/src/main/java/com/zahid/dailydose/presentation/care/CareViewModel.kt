package com.zahid.dailydose.presentation.care

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.zahid.dailydose.domain.model.HealthMetric
import com.zahid.dailydose.domain.model.HealthMetricType
import com.zahid.dailydose.domain.model.HealthMetricSummary
import com.zahid.dailydose.domain.model.Patient
import com.zahid.dailydose.domain.repository.HealthMetricRepository
import com.zahid.dailydose.domain.repository.PatientRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


class CareViewModel(val supabaseClient: SupabaseClient) : ViewModel(), KoinComponent {
    private val healthMetricRepository: HealthMetricRepository by inject()
    private val patientRepository: PatientRepository by inject()
    private val _uiState = MutableStateFlow(CareUiState())
    val uiState: StateFlow<CareUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CareEvents>()
    val effects = _effects.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()?.id
                if (currentUser != null) {
                    // Load patient data for emergency contact
                    loadProfile(currentUser)

                    // Load health metrics
                    val metricsResult = healthMetricRepository.getLatestHealthMetrics(currentUser)
                    metricsResult.onSuccess { metrics ->
                        _uiState.value = _uiState.value.copy(healthMetrics = metrics)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun loadAllData(type: HealthMetricType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()?.id
                if (currentUser != null) {
                    // Load patient data for emergency contact
                    loadProfile(currentUser)

                    // Load health metrics
                    val metricsResult = healthMetricRepository.getHealthMetricsByType(currentUser,type)
                    metricsResult.onSuccess { metrics ->
                        _uiState.value = _uiState.value.copy(allMetrics = metrics)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun loadProfile(userId: String){
        viewModelScope.launch {
            val patientResult = patientRepository.getPatient(userId)
            patientResult.onSuccess { patient ->
                _uiState.value = _uiState.value.copy(patient = patient)
            }
        }
    }

    fun addHealthMetric(
        type: HealthMetricType,
        value: Double,
        unit: String,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val currentUser = supabaseClient.auth.currentUserOrNull() ?: return@launch

            val metric = HealthMetric(
                id = UUID.randomUUID().toString(),
                userId = currentUser.id,
                type = type,
                value = value,
                unit = unit,
                notes = notes,
                recordedAt = Date().time,
                createdAt = Date().time,
                updatedAt = Date().time
            )

            val result = healthMetricRepository.addHealthMetric(metric)
            if (result.isSuccess) {
                _effects.send(CareEvents.OnAddedNew)
            } else {
                _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refresh() {
        loadData()
    }

    fun exportToPdf(
        context: Context,
        metricType: HealthMetricType,
        startDate: Date,
        endDate: Date
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val currentUser = supabaseClient.auth.currentUserOrNull()?.id
                if (currentUser != null) {
                    val metricsResult = healthMetricRepository.getHealthMetricsByType(currentUser, metricType)
                    metricsResult.onSuccess { allMetrics ->
                        // Filter metrics by date range
                        val filteredMetrics = allMetrics.filter { metric ->
                            val metricDate = Date(metric.recordedAt)
                            metricDate.time >= startDate.time && metricDate.time <= endDate.time
                        }.sortedByDescending { it.recordedAt }

                        if (filteredMetrics.isNotEmpty()) {
                            val file = generatePdf(context, metricType, filteredMetrics, startDate, endDate)
                            _effects.send(CareEvents.OnPdfExported(file))
                        } else {
                            _effects.send(CareEvents.OnExportError("No data found for the selected date range"))
                        }
                    }.onFailure { error ->
                        _effects.send(CareEvents.OnExportError(error.message ?: "Export failed"))
                    }
                }
            } catch (e: Exception) {
                _effects.send(CareEvents.OnExportError(e.message ?: "Export failed"))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun generatePdf(
        context: Context,
        metricType: HealthMetricType,
        metrics: List<HealthMetric>,
        startDate: Date,
        endDate: Date
    ): File = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val title = when (metricType) {
            HealthMetricType.BLOOD_PRESSURE_SYSTOLIC -> "Blood Pressure (Systolic)"
            HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> "Blood Pressure (Diastolic)"
            HealthMetricType.HEART_RATE -> "Heart Rate"
            HealthMetricType.WEIGHT -> "Weight"
            HealthMetricType.TEMPERATURE -> "Temperature"
            HealthMetricType.DIABETES -> "Diabetes"
        }

        val fileName = "${title.replace(" ", "_")}_${fileDateFormat.format(startDate)}_to_${fileDateFormat.format(endDate)}.pdf"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        val writer = PdfWriter(FileOutputStream(file))
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)
        
        // Create bold font
        val boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
        val normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)

        // Title
        val titleParagraph = Paragraph("$title History")
            .setFontSize(20f)
            .setFont(boldFont)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(10f)
        document.add(titleParagraph)

        // Date range
        val dateRangeParagraph = Paragraph(
            "From ${dateFormat.format(startDate)} to ${dateFormat.format(endDate)}"
        )
            .setFontSize(12f)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20f)
        document.add(dateRangeParagraph)

        // Table
        val table = Table(UnitValue.createPercentArray(floatArrayOf(3f, 2f, 3f, 2f)))
            .useAllAvailableWidth()

        // Table headers
        table.addHeaderCell(
            Cell().add(Paragraph("Value").setFont(boldFont))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
        )
        table.addHeaderCell(
            Cell().add(Paragraph("Unit").setFont(boldFont))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
        )
        table.addHeaderCell(
            Cell().add(Paragraph("Date & Time").setFont(boldFont))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
        )
        table.addHeaderCell(
            Cell().add(Paragraph("Notes").setFont(boldFont))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
        )

        // Table data
        metrics.forEach { metric ->
            table.addCell(Cell().add(Paragraph(metric.value.toString())))
            table.addCell(Cell().add(Paragraph(metric.unit)))
            table.addCell(Cell().add(Paragraph(dateFormat.format(Date(metric.recordedAt)))))
            table.addCell(Cell().add(Paragraph(metric.notes ?: "")))
        }

        document.add(table)

        // Summary
        val summaryParagraph = Paragraph("\nSummary:")
            .setFontSize(14f)
            .setFont(boldFont)
            .setMarginTop(20f)
        document.add(summaryParagraph)

        val avgValue = metrics.map { it.value }.average()
        val maxValue = metrics.maxOf { it.value }
        val minValue = metrics.minOf { it.value }

        val statsTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f)))
            .useAllAvailableWidth()
            .setMarginTop(10f)

        statsTable.addCell(Cell().add(Paragraph("Total Readings:").setFont(boldFont)))
        statsTable.addCell(Cell().add(Paragraph(metrics.size.toString()).setFont(normalFont)))
        
        statsTable.addCell(Cell().add(Paragraph("Average Value:").setFont(boldFont)))
        statsTable.addCell(Cell().add(Paragraph("${"%.2f".format(avgValue)} ${metrics.first().unit}").setFont(normalFont)))
        
        statsTable.addCell(Cell().add(Paragraph("Highest Value:").setFont(boldFont)))
        statsTable.addCell(Cell().add(Paragraph("$maxValue ${metrics.first().unit}").setFont(normalFont)))
        
        statsTable.addCell(Cell().add(Paragraph("Lowest Value:").setFont(boldFont)))
        statsTable.addCell(Cell().add(Paragraph("$minValue ${metrics.first().unit}").setFont(normalFont)))

        document.add(statsTable)

        document.close()
        file
    }
}

data class CareUiState(
    val isLoading: Boolean = false,
    val patient: Patient? = null,
    val healthMetrics: List<HealthMetricSummary> = emptyList(),
    val allMetrics : List<HealthMetric> = emptyList(),
    val error: String? = null,
    val isAddedNew: Boolean = false
)

sealed class CareEvents {
    object OnAddedNew : CareEvents()
    data class OnPdfExported(val file: File) : CareEvents()
    data class OnExportError(val message: String) : CareEvents()
}
