package com.studentos.app.ui.screen.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentos.app.domain.model.Student
import com.studentos.app.domain.repository.StudentRepository
import com.studentos.app.ui.components.*
import com.studentos.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val currentStep: Int = 0,
    val name: String = "",
    val university: String = "",
    val faculty: String = "",
    val course: Int = 1,
    val group: String = "",
    val semester: Int = 1,
    val isComplete: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val studentRepo: StudentRepository
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun updateName(name: String) { _state.value = _state.value.copy(name = name) }
    fun updateUniversity(uni: String) { _state.value = _state.value.copy(university = uni) }
    fun updateFaculty(fac: String) { _state.value = _state.value.copy(faculty = fac) }
    fun updateCourse(course: Int) { _state.value = _state.value.copy(course = course) }
    fun updateGroup(group: String) { _state.value = _state.value.copy(group = group) }
    fun updateSemester(semester: Int) { _state.value = _state.value.copy(semester = semester) }
    fun nextStep() { _state.value = _state.value.copy(currentStep = _state.value.currentStep + 1) }
    fun prevStep() { _state.value = _state.value.copy(currentStep = maxOf(0, _state.value.currentStep - 1)) }

    fun skipAndFinish() {
        viewModelScope.launch {
            studentRepo.save(Student(name = "Student", semester = _state.value.semester))
            _state.value = _state.value.copy(isComplete = true)
        }
    }

    fun finish() {
        viewModelScope.launch {
            val s = _state.value
            studentRepo.save(
                Student(
                    name = s.name.ifBlank { "Student" },
                    university = s.university,
                    faculty = s.faculty,
                    course = s.course,
                    group = s.group,
                    semester = s.semester
                )
            )
            _state.value = _state.value.copy(isComplete = true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 4 })

    LaunchedEffect(state.currentStep) {
        pagerState.animateScrollToPage(state.currentStep)
    }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onComplete()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(ContentPadding.horizontal)
        ) {
            // Top bar with skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.currentStep > 0) {
                    IconButton(onClick = { viewModel.prevStep() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                TextButton(onClick = { viewModel.skipAndFinish() }) {
                    Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Progress indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.md),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .width(if (index == state.currentStep) 24.dp else 8.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (index <= state.currentStep) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> StudentInfoPage(state, viewModel)
                    2 -> SemesterPage(state, viewModel)
                    3 -> DonePage()
                }
            }

            // Bottom button
            AppButton(
                text = when (state.currentStep) {
                    0 -> "Get Started"
                    3 -> "Start Using StudentOS"
                    else -> "Continue"
                },
                onClick = {
                    if (state.currentStep >= 3) {
                        viewModel.finish()
                    } else {
                        viewModel.nextStep()
                    }
                },
                style = ButtonStyle.Primary,
                fullWidth = true,
                modifier = Modifier.padding(vertical = Spacing.lg),
                enabled = when (state.currentStep) {
                    1 -> state.name.isNotBlank()
                    else -> true
                }
            )
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Spacing.xxl))

        Icon(
            Icons.Outlined.School,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
        Text(
            text = "Welcome to StudentOS",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = "Your personal academic operating system.\nTrack classes, tasks, grades, and more — all offline, all private.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StudentInfoPage(state: OnboardingState, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.base)
    ) {
        Text(
            text = "About You",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Let's personalize your experience.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        OutlinedTextField(
            value = state.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("Your Name *") },
            placeholder = { Text("Ivan Petrov") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            shape = RoundedCornerShape(AppRadius.md)
        )

        OutlinedTextField(
            value = state.university,
            onValueChange = { viewModel.updateUniversity(it) },
            label = { Text("University") },
            placeholder = { Text("Moscow State University") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(AppRadius.md)
        )

        OutlinedTextField(
            value = state.faculty,
            onValueChange = { viewModel.updateFaculty(it) },
            label = { Text("Faculty") },
            placeholder = { Text("Computer Science") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(AppRadius.md)
        )

        OutlinedTextField(
            value = state.group,
            onValueChange = { viewModel.updateGroup(it) },
            label = { Text("Group") },
            placeholder = { Text("CS-201") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(AppRadius.md)
        )
    }
}

@Composable
private fun SemesterPage(state: OnboardingState, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.base)
    ) {
        Text(
            text = "Academic Details",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "You can always change these later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text("Course", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            (1..6).forEach { course ->
                FilterChip(
                    selected = state.course == course,
                    onClick = { viewModel.updateCourse(course) },
                    label = { Text("$course") },
                    shape = RoundedCornerShape(AppRadius.full)
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        Text("Semester", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            (1..2).forEach { semester ->
                FilterChip(
                    selected = state.semester == semester,
                    onClick = { viewModel.updateSemester(semester) },
                    label = { Text("Semester $semester") },
                    shape = RoundedCornerShape(AppRadius.full)
                )
            }
        }
    }
}

@Composable
private fun DonePage() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Spacing.xxl))

        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = AppTheme.extendedColors.success
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
        Text(
            text = "You're All Set!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = "Start adding subjects, schedule, and tasks.\nEverything works offline. Your data stays on your device.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
