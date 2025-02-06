import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.test.MavericksTestRule
import com.airbnb.mvrx.withState
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.PageView
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.content.shareextension.ExtensionViewModel
import com.alfresco.content.shareextension.ExtensionViewState
import com.alfresco.content.shareextension.R
import com.alfresco.content.shareextension.getResourceList
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class ExtensionViewModelTest {

    @get:Rule
    val mavericksRule = MavericksTestRule()

    private lateinit var viewModel: ExtensionViewModel

    private val context: Context = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)

    private val mockSession: Session = mockk(relaxed = true) // Mock Session
    private val browseRepository: BrowseRepository = BrowseRepository(mockSession) // Pass mock session

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // Initialize MockK annotations
        MockKAnnotations.init(this)
        MockitoAnnotations.openMocks(this)

        // Mock SessionManager to return our mock session
        mockkObject(SessionManager)
        every { SessionManager.requireSession } returns mockSession

        // Override Dispatchers.Main for testing coroutines
        Dispatchers.setMain(testDispatcher)

        // Initialize Mavericks
        Mavericks.initialize(context)


        // Mock resources in context
        every { context.resources } returns resources

        // Make sure the static method is mocked before usage
        mockkStatic("com.alfresco.content.shareextension.ExtensionViewModelKt")

        // Mock resource arrays
        every { resources.getStringArray(R.array.share_menu_titles) } returns arrayOf("Personal", "Libraries")
        every { resources.getStringArray(R.array.share_menu_paths) } returns arrayOf("/personal", "/libraries")

        // Ensure `getResourceList` is properly mocked
        every { resources.getResourceList(R.array.share_menu_icons) } returns mutableListOf(1, 2)

        // Debug statements
        println("Mocked Titles: ${resources.getStringArray(R.array.share_menu_titles).toList()}")
        println("Mocked Icons: ${resources.getResourceList(R.array.share_menu_icons)}")
        println("Mocked Paths: ${resources.getStringArray(R.array.share_menu_paths).toList()}")

    }


    @Test
    fun `viewModel should initialize with menu entries`() {
        viewModel = ExtensionViewModel(
            ExtensionViewState(path = "/"),
            context
        )

        withState(viewModel) { state ->
            Assert.assertEquals(2, state.entries.size)
            Assert.assertEquals("/personal", state.entries[0].path)
            Assert.assertEquals("Personal", state.entries[0].title)
        }
    }

    @Test
    fun `getMyFilesNodeId should return BrowseRepository nodeId`() {

        // Create ViewModel
        viewModel = ExtensionViewModel(
            ExtensionViewState(path = "/"),
            context
        )

        // Arrange
        every { browseRepository.myFilesNodeId } returns "12345"

        // Act
        val nodeId = viewModel.getMyFilesNodeId()

        // Assert
        assertEquals("12345", nodeId)
    }

    @Test
    fun `viewModel should initialize with empty entries if resources are missing`() {
        every { resources.getStringArray(R.array.share_menu_titles) } returns emptyArray()
        every { resources.getStringArray(R.array.share_menu_paths) } returns emptyArray()
        every { resources.getResourceList(R.array.share_menu_icons) } returns mutableListOf()

        viewModel = ExtensionViewModel(
            ExtensionViewState(path = "/"),
            context
        )

        withState(viewModel) { state ->
            Assert.assertTrue(state.entries.isEmpty())
        }
    }

    @Test
    fun `getPageView should return correct PageView for known paths`() {
        val mockTypedArray: TypedArray = mockk(relaxed = true)

        val mockResources: Resources = mockk {
            every { getString(R.string.browse_menu_personal) } returns "Personal files"
            every { getString(R.string.browse_menu_my_libraries) } returns "My libraries"
            every { getStringArray(any()) } returns arrayOf()
            every { obtainTypedArray(any()) } returns mockTypedArray
        }

        val innerContext: Context = mockk {
            every { resources } returns mockResources
            every { getString(R.string.browse_menu_personal) } answers { mockResources.getString(R.string.browse_menu_personal) }
            every { getString(R.string.browse_menu_my_libraries) } answers { mockResources.getString(R.string.browse_menu_my_libraries) }
        }

        viewModel = ExtensionViewModel(
            ExtensionViewState(path = "/"),
            innerContext
        )

        val personalPageView = viewModel.getPageView(innerContext.getString(R.string.browse_menu_personal))

        assertEquals(PageView.PersonalFiles, personalPageView)

        val librariesPageView = viewModel.getPageView(innerContext.getString(R.string.browse_menu_my_libraries))
        assertEquals(PageView.MyLibraries, librariesPageView)
    }




    @Test
    fun `getPageView should return PageView None for unknown path`() {
        viewModel = ExtensionViewModel(
            ExtensionViewState(path = "/"),
            context
        )

        val unknownPageView = viewModel.run { getPageView("Unknown Path") }
        assertEquals(PageView.None, unknownPageView)
    }

    @Test
    fun `getMyFilesNodeId should return correct node ID from repository`() {
        every { browseRepository.myFilesNodeId } returns "67890"

        viewModel = ExtensionViewModel(
            ExtensionViewState(path = "/"),
            context
        )

        val nodeId = viewModel.getMyFilesNodeId()
        assertEquals("67890", nodeId)
    }

    @Test
    fun `viewModel should handle missing resources gracefully`() {
        every { resources.getStringArray(any()) } returns emptyArray()
        every { resources.getResourceList(any()) } returns mutableListOf()

        viewModel = ExtensionViewModel(
            ExtensionViewState(path = "/"),
            context
        )

        withState(viewModel) { state ->
            assertEquals(0, state.entries.size)
        }
    }

}
