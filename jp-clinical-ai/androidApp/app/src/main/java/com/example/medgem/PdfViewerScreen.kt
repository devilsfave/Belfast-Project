package com.example.medgem

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableTarget
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.pdf.viewer.fragment.PdfViewerFragment
import com.example.medgem.ui.components.MedGemTopBar
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Custom PDF viewer fragment that jumps to a specific page after loading.
 */
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 13)
@SuppressLint("RestrictedApi")
class SafePdfViewerFragment : PdfViewerFragment() {
    private var initialPage: Int = 0
    private var searchQuery: String? = null

    companion object {
        private const val ARG_PAGE = "initial_page"
        private const val ARG_QUERY = "search_query"

        fun newInstance(uri: Uri, page: Int, query: String? = null): SafePdfViewerFragment {
            return SafePdfViewerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("documentUri", uri)
                    putInt(ARG_PAGE, page)
                    if (query != null) {
                        putString(ARG_QUERY, query)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialPage = arguments?.getInt(ARG_PAGE) ?: 0
        searchQuery = arguments?.getString(ARG_QUERY)
    }

    @SuppressLint("VisibleForTests")
    override fun onLoadDocumentSuccess() {
        super.onLoadDocumentSuccess()
        if (initialPage > 0) {
            // Post to view queue to ensure PdfView is fully ready and laid out
            pdfView.post {
                try {
                    pdfView.scrollToPage(initialPage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Enable text search if we have a query (best effort)
        if (!searchQuery.isNullOrEmpty()) {
            isTextSearchActive = true
        }
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 13)
@OptIn(ExperimentalMaterial3Api::class)
@ComposableTarget("androidx.compose.ui.UiComposable")
@Composable
fun PdfViewerScreen(
    pdfFileName: String,
    pageNumber: Int,
    searchQuery: String? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val fragmentActivity = remember(context) { context as? FragmentActivity }
    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    val containerId = remember { View.generateViewId() }

    // Copy asset to cache on launch
    LaunchedEffect(pdfFileName) {
        try {
            val cacheFile = File(context.cacheDir, pdfFileName)
            // Ensure parent directories exist in cache
            cacheFile.parentFile?.mkdirs()

            context.assets.open("pdfs/$pdfFileName").use { input ->
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }
            pdfUri = Uri.fromFile(cacheFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val displayTitle = formatPdfDisplayName(pdfFileName)

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        MedGemTopBar(
            title = displayTitle,
            onBack = onBack
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
        ) {
            if (pdfUri != null && fragmentActivity != null) {
                var fragmentAdded by remember { mutableStateOf(false) }

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        FragmentContainerView(ctx).apply {
                            id = containerId
                        }
                    },
                    update = { _ ->
                        if (!fragmentAdded) {
                            fragmentActivity.supportFragmentManager.commit {
                                replace(
                                    containerId,
                                    SafePdfViewerFragment.newInstance(
                                        pdfUri!!,
                                        pageNumber,
                                        searchQuery
                                    )
                                )
                            }
                            fragmentAdded = true
                        }
                    }
                )
            } else if (pdfUri == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Loading PDF...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

// Helper to format title nicely
fun formatPdfDisplayName(fileName: String): String {
    return fileName.substringAfterLast("/")
        .removeSuffix(".pdf")
        .replace("-", " ")
        .replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
}
