package com.arygm.quickfix.model.account

import android.graphics.Bitmap
import android.net.Uri
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import junit.framework.TestCase.fail
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class AccountRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @Mock private lateinit var mockDocumentReference: DocumentReference

  @Mock private lateinit var mockCollectionReference: CollectionReference

  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot

  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot

  @Mock private lateinit var mockAccountQuerySnapshot: QuerySnapshot

  @Mock private lateinit var mockQuery: Query
  // Firebase Storage Mocks
  @Mock private lateinit var mockStorage: FirebaseStorage
  @Mock private lateinit var storageRef: StorageReference
  @Mock private lateinit var storageRef1: StorageReference
  @Mock private lateinit var storageRef2: StorageReference
  @Mock private lateinit var accountFolderRef: StorageReference

  private lateinit var mockFirebaseAuth: FirebaseAuth
  private lateinit var firebaseAuthMockedStatic: MockedStatic<FirebaseAuth>

  private lateinit var accountRepositoryFirestore: AccountRepositoryFirestore

  private val account =
      Account(
          uid = "1",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          birthDate = Timestamp.now(),
          isWorker = false,
          profilePicture = "https://example.com/profile.jpg")

  private val account2 =
      Account(
          uid = "2",
          firstName = "Jane",
          lastName = "Smith",
          email = "jane.smith@example.com",
          birthDate = Timestamp.now(),
          isWorker = true,
          profilePicture = "https://example.com/profile2.jpg")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    firebaseAuthMockedStatic = Mockito.mockStatic(FirebaseAuth::class.java)
    mockFirebaseAuth = Mockito.mock(FirebaseAuth::class.java)

    // Mock FirebaseAuth.getInstance() to return the mockFirebaseAuth
    firebaseAuthMockedStatic
        .`when`<FirebaseAuth> { FirebaseAuth.getInstance() }
        .thenReturn(mockFirebaseAuth)
    `when`(mockStorage.reference).thenReturn(storageRef)
    `when`(storageRef.child(anyString())).thenReturn(storageRef1)
    `when`(storageRef1.child(anyString())).thenReturn(storageRef2)
    `when`(storageRef2.child(anyString())).thenReturn(accountFolderRef)
    accountRepositoryFirestore = AccountRepositoryFirestore(mockFirestore, mockStorage)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockAccountQuerySnapshot))

    // **Correctly mock CollectionReference.whereEqualTo() with specific field**
    `when`(mockCollectionReference.whereEqualTo(eq("email"), any<String>())).thenReturn(mockQuery)

    // Mock Query.get() to return the desired Task
    `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
  }

  @After
  fun tearDown() {
    // Close the static mock
    firebaseAuthMockedStatic.close()
  }

  @Test
  fun uploadAccountImages_whenSuccess_callsOnSuccess() {
    val accountId = "1"
    val bitmaps = listOf(mock(Bitmap::class.java))
    val expectedUrl = listOf("https://example.com/uploaded_image.jpg")
    val baos = ByteArrayOutputStream()

    // Simuler la compression d'image
    bitmaps.forEach { bitmap -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos) }
    val imageData = baos.toByteArray()

    // Mock StorageReference
    `when`(storageRef.child("accounts").child(accountId)).thenReturn(accountFolderRef)
    val fileRef = mock(StorageReference::class.java)

    `when`(accountFolderRef.child(anyString())).thenReturn(fileRef)

    // Mock putBytes
    val mockUploadTask = mock(UploadTask::class.java)
    `when`(fileRef.putBytes(eq(imageData))).thenReturn(mockUploadTask)

    // Mock addOnSuccessListener
    `when`(mockUploadTask.addOnSuccessListener(org.mockito.kotlin.any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<UploadTask.TaskSnapshot>>(0)
      val taskSnapshot = mock(UploadTask.TaskSnapshot::class.java) // Mock the snapshot
      listener.onSuccess(taskSnapshot)
      mockUploadTask
    }

    // Mock fileRef.downloadUrl
    `when`(fileRef.downloadUrl).thenReturn(Tasks.forResult(Uri.parse(expectedUrl[0])))

    var resultUrl = listOf<String>()
    accountRepositoryFirestore.uploadAccountImages(
        accountId = accountId,
        images = bitmaps,
        onSuccess = { urls ->
          resultUrl = urls
          assertEquals(expectedUrl, resultUrl)
        },
        onFailure = { fail("Failure callback should not be called") })
    shadowOf(Looper.getMainLooper()).idle()
  }

  @Test
  fun uploadAccountImages_whenFailure_callsOnFailure() {
    val accountId = "1"
    val bitmaps = listOf(mock(Bitmap::class.java))
    val exception = Exception("Upload failed")

    // Mock StorageReference
    `when`(storageRef.child("accounts").child(accountId)).thenReturn(accountFolderRef)
    val fileRef = mock(StorageReference::class.java)
    `when`(accountFolderRef.child(anyString())).thenReturn(fileRef)

    // Mock putBytes
    val mockUploadTask = mock(UploadTask::class.java)
    `when`(fileRef.putBytes(any())).thenReturn(mockUploadTask)

    // Mock addOnFailureListener
    `when`(mockUploadTask.addOnFailureListener(org.mockito.kotlin.any())).thenAnswer { invocation ->
      val listener = invocation.arguments[0] as OnFailureListener
      listener.onFailure(exception) // Simule une erreur d'upload
      mockUploadTask
    }

    // Mock addOnSuccessListener pour éviter des comportements inattendus
    `when`(mockUploadTask.addOnSuccessListener(org.mockito.kotlin.any())).thenReturn(mockUploadTask)

    // Act
    var onFailureCalled = false
    var exceptionReceived: Exception? = null
    accountRepositoryFirestore.uploadAccountImages(
        accountId = accountId,
        images = bitmaps,
        onSuccess = { fail("onSuccess should not be called when upload fails") },
        onFailure = { e ->
          onFailureCalled = true
          exceptionReceived = e
        })

    // Attendre que les tâches se terminent
    shadowOf(Looper.getMainLooper()).idle()

    // Assertions
    assertTrue(onFailureCalled)
    assertNotNull(exceptionReceived)
    assertEquals(exception, exceptionReceived)
  }

  // ----- CRUD Operation Tests -----

  @Test
  fun getAccounts_callsDocuments() {
    `when`(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockAccountQuerySnapshot))
    `when`(mockAccountQuerySnapshot.documents).thenReturn(listOf())

    accountRepositoryFirestore.getAccounts(
        onSuccess = {}, onFailure = { fail("Failure callback should not be called") })

    verify(mockCollectionReference).get()
  }

  @Test
  fun addAccount_shouldCallFirestoreCollection() {
    `when`(mockDocumentReference.set(any<Account>())).thenReturn(Tasks.forResult(null))

    accountRepositoryFirestore.addAccount(account, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun addAccount_whenSuccess_callsOnSuccess() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.set(any<Account>())).thenReturn(taskCompletionSource.task)

    var callbackCalled = false

    accountRepositoryFirestore.addAccount(
        account = account,
        onSuccess = { callbackCalled = true },
        onFailure = { fail("Failure callback should not be called") })

    taskCompletionSource.setResult(null)

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(callbackCalled)
  }

  @Test
  fun addAccount_whenFailure_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.set(any<Account>())).thenReturn(taskCompletionSource.task)

    val exception = Exception("Test exception")
    var callbackCalled = false
    var returnedException: Exception? = null

    accountRepositoryFirestore.addAccount(
        account = account,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { e ->
          callbackCalled = true
          returnedException = e
        })

    taskCompletionSource.setException(exception)

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(callbackCalled)
    assertEquals(exception, returnedException)
  }

  @Test
  fun updateAccount_shouldCallFirestoreCollection() {
    `when`(mockDocumentReference.set(any<Account>())).thenReturn(Tasks.forResult(null))

    accountRepositoryFirestore.updateAccount(account, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
  }

  @Test
  fun updateAccount_whenSuccess_callsOnSuccess() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.set(any<Account>())).thenReturn(taskCompletionSource.task)

    var callbackCalled = false

    accountRepositoryFirestore.updateAccount(
        account = account,
        onSuccess = { callbackCalled = true },
        onFailure = { fail("Failure callback should not be called") })

    taskCompletionSource.setResult(null)

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(callbackCalled)
  }

  @Test
  fun updateAccount_whenFailure_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.set(any<Account>())).thenReturn(taskCompletionSource.task)

    val exception = Exception("Test exception")
    var callbackCalled = false
    var returnedException: Exception? = null

    accountRepositoryFirestore.updateAccount(
        account = account,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { e ->
          callbackCalled = true
          returnedException = e
        })

    taskCompletionSource.setException(exception)

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(callbackCalled)
    assertEquals(exception, returnedException)
  }

  @Test
  fun deleteAccountById_shouldCallDocumentReferenceDelete() {
    `when`(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    accountRepositoryFirestore.deleteAccountById("1", onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).delete()
  }

  @Test
  fun deleteAccountById_whenSuccess_callsOnSuccess() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.delete()).thenReturn(taskCompletionSource.task)

    var callbackCalled = false

    accountRepositoryFirestore.deleteAccountById(
        id = "1",
        onSuccess = { callbackCalled = true },
        onFailure = { fail("Failure callback should not be called") })

    taskCompletionSource.setResult(null)

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(callbackCalled)
  }

  @Test
  fun deleteAccountById_whenFailure_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<Void>()
    `when`(mockDocumentReference.delete()).thenReturn(taskCompletionSource.task)

    val exception = Exception("Test exception")
    var callbackCalled = false
    var returnedException: Exception? = null

    accountRepositoryFirestore.deleteAccountById(
        id = "1",
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { e ->
          callbackCalled = true
          returnedException = e
        })

    taskCompletionSource.setException(exception)

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(callbackCalled)
    assertEquals(exception, returnedException)
  }

  // ----- getAccountById Tests -----

  @Test
  fun getAccountById_whenDocumentExists_callsOnSuccessWithAccount() {
    val uid = "1"

    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.exists()).thenReturn(true)

    // Mocking the data returned from Firestore
    `when`(mockDocumentSnapshot.id).thenReturn(account.uid)
    `when`(mockDocumentSnapshot.getString("firstName")).thenReturn(account.firstName)
    `when`(mockDocumentSnapshot.getString("lastName")).thenReturn(account.lastName)
    `when`(mockDocumentSnapshot.getString("email")).thenReturn(account.email)
    `when`(mockDocumentSnapshot.getTimestamp("birthDate")).thenReturn(account.birthDate)
    `when`(mockDocumentSnapshot.getBoolean("worker")).thenReturn(account.isWorker)
    `when`(mockDocumentSnapshot.getString("profilePicture")).thenReturn(account.profilePicture)

    var callbackCalled = false

    accountRepositoryFirestore.getAccountById(
        uid = uid,
        onSuccess = { foundAccount ->
          callbackCalled = true
          assertEquals(account, foundAccount)
        },
        onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(callbackCalled)
  }

  @Test
  fun getAccountById_whenDocumentDoesNotExist_callsOnSuccessWithNull() {
    val uid = "nonexistent"

    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.exists()).thenReturn(false)

    var callbackCalled = false

    accountRepositoryFirestore.getAccountById(
        uid = uid,
        onSuccess = { foundAccount ->
          callbackCalled = true
          assertNull(foundAccount)
        },
        onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(callbackCalled)
  }

  @Test
  fun getAccountById_whenFailure_callsOnFailure() {
    val uid = "1"
    val exception = Exception("Test exception")

    `when`(mockDocumentReference.get()).thenReturn(Tasks.forException(exception))

    var failureCallbackCalled = false

    accountRepositoryFirestore.getAccountById(
        uid = uid,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { e ->
          failureCallbackCalled = true
          assertEquals(exception, e)
        })

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(failureCallbackCalled)
  }

  // ----- accountExists Tests -----

  @Test
  fun accountExists_whenAccountExists_callsOnSuccessWithTrueAndAccount() {
    val email = "john.doe@example.com"

    `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockAccountQuerySnapshot))
    `when`(mockAccountQuerySnapshot.isEmpty).thenReturn(false)
    `when`(mockAccountQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    `when`(mockDocumentSnapshot.id).thenReturn(account.uid)
    `when`(mockDocumentSnapshot.getString("firstName")).thenReturn(account.firstName)
    `when`(mockDocumentSnapshot.getString("lastName")).thenReturn(account.lastName)
    `when`(mockDocumentSnapshot.getString("email")).thenReturn(account.email)
    `when`(mockDocumentSnapshot.getTimestamp("birthDate")).thenReturn(account.birthDate)
    `when`(mockDocumentSnapshot.getBoolean("worker")).thenReturn(account.isWorker)
    `when`(mockDocumentSnapshot.getString("profilePicture")).thenReturn(account.profilePicture)

    var callbackCalled = false

    accountRepositoryFirestore.accountExists(
        email = email,
        onSuccess = { (exists, foundAccount) ->
          callbackCalled = true
          assertTrue(exists)
          assertEquals(account, foundAccount)
        },
        onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(callbackCalled)
  }

  @Test
  fun accountExists_whenAccountDoesNotExist_callsOnSuccessWithFalseAndNull() {
    val email = "unknown@example.com"

    `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockAccountQuerySnapshot))
    `when`(mockAccountQuerySnapshot.isEmpty).thenReturn(true)

    var callbackCalled = false

    accountRepositoryFirestore.accountExists(
        email = email,
        onSuccess = { (exists, foundAccount) ->
          callbackCalled = true
          assertFalse(exists)
          assertNull(foundAccount)
        },
        onFailure = { fail("Failure callback should not be called") })

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(callbackCalled)
  }

  @Test
  fun accountExists_whenFailure_callsOnFailure() {
    val email = "john.doe@example.com"
    val exception = Exception("Test exception")

    `when`(mockQuery.get()).thenReturn(Tasks.forException(exception))

    var failureCallbackCalled = false

    accountRepositoryFirestore.accountExists(
        email = email,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { e ->
          failureCallbackCalled = true
          assertEquals(exception, e)
        })

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(failureCallbackCalled)
  }

  // ----- getAccounts Tests -----

  @Test
  fun getAccounts_whenSuccess_callsOnSuccessWithAccounts() {
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()
    `when`(mockCollectionReference.get()).thenReturn(taskCompletionSource.task)

    val document1 = Mockito.mock(DocumentSnapshot::class.java)
    val document2 = Mockito.mock(DocumentSnapshot::class.java)

    `when`(mockAccountQuerySnapshot.documents).thenReturn(listOf(document1, document2))

    // Mock data for first document
    `when`(document1.id).thenReturn(account.uid)
    `when`(document1.getString("firstName")).thenReturn(account.firstName)
    `when`(document1.getString("lastName")).thenReturn(account.lastName)
    `when`(document1.getString("email")).thenReturn(account.email)
    `when`(document1.getTimestamp("birthDate")).thenReturn(account.birthDate)
    `when`(document1.getBoolean("worker")).thenReturn(account.isWorker)
    `when`(document1.getString("profilePicture")).thenReturn(account.profilePicture)

    // Mock data for second document
    `when`(document2.id).thenReturn(account2.uid)
    `when`(document2.getString("firstName")).thenReturn(account2.firstName)
    `when`(document2.getString("lastName")).thenReturn(account2.lastName)
    `when`(document2.getString("email")).thenReturn(account2.email)
    `when`(document2.getTimestamp("birthDate")).thenReturn(account2.birthDate)
    `when`(document2.getBoolean("worker")).thenReturn(account2.isWorker)
    `when`(document2.getString("profilePicture")).thenReturn(account2.profilePicture)

    var callbackCalled = false
    var returnedAccounts: List<Account>? = null

    accountRepositoryFirestore.getAccounts(
        onSuccess = { fetchedAccounts ->
          callbackCalled = true
          returnedAccounts = fetchedAccounts
        },
        onFailure = { fail("Failure callback should not be called") })

    taskCompletionSource.setResult(mockAccountQuerySnapshot)

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(callbackCalled)
    assertNotNull(returnedAccounts)
    assertEquals(2, returnedAccounts!!.size)
    assertEquals(account, returnedAccounts!![0])
    assertEquals(account2, returnedAccounts!![1])
  }

  @Test
  fun getAccounts_whenFailure_callsOnFailure() {
    val taskCompletionSource = TaskCompletionSource<QuerySnapshot>()
    `when`(mockCollectionReference.get()).thenReturn(taskCompletionSource.task)

    val exception = Exception("Test exception")

    var callbackCalled = false
    var returnedException: Exception? = null

    accountRepositoryFirestore.getAccounts(
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { e ->
          callbackCalled = true
          returnedException = e
        })

    taskCompletionSource.setException(exception)

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(callbackCalled)
    assertEquals(exception, returnedException)
  }

  // ----- documentToAccount Tests -----

  @Test
  fun documentToAccount_whenAllFieldsArePresent_returnsAccount() {
    // Arrange
    val document = Mockito.mock(DocumentSnapshot::class.java)
    `when`(document.id).thenReturn(account.uid)
    `when`(document.getString("firstName")).thenReturn(account.firstName)
    `when`(document.getString("lastName")).thenReturn(account.lastName)
    `when`(document.getString("email")).thenReturn(account.email)
    `when`(document.getTimestamp("birthDate")).thenReturn(account.birthDate)
    `when`(document.getBoolean("worker")).thenReturn(account.isWorker)
    `when`(document.getString("profilePicture")).thenReturn(account.profilePicture)

    // Act
    val result = invokeDocumentToAccount(document)

    // Assert
    assertNotNull(result)
    assertEquals(account, result)
  }

  @Test
  fun documentToAccount_whenEssentialFieldsAreMissing_returnsNull() {
    // Arrange
    val document = Mockito.mock(DocumentSnapshot::class.java)
    `when`(document.id).thenReturn(account.uid)
    // Missing "firstName", "lastName", "email", "birthDate"
    `when`(document.getString("firstName")).thenReturn(null)
    `when`(document.getString("lastName")).thenReturn(null)
    `when`(document.getString("email")).thenReturn(null)
    `when`(document.getTimestamp("birthDate")).thenReturn(null)

    // Act
    val result = invokeDocumentToAccount(document)

    // Assert
    assertNull(result)
  }

  @Test
  fun documentToAccount_whenInvalidDataType_returnsNull() {
    // Arrange
    val document = Mockito.mock(DocumentSnapshot::class.java)
    `when`(document.id).thenReturn(account.uid)
    `when`(document.getString("firstName")).thenReturn(account.firstName)
    `when`(document.getString("lastName")).thenReturn(account.lastName)
    `when`(document.getString("email")).thenReturn(account.email)
    // "birthDate" field has invalid data type (e.g., String instead of Timestamp)
    `when`(document.getTimestamp("birthDate")).thenReturn(null)

    // Act
    val result = invokeDocumentToAccount(document)

    // Assert
    assertNull(result)
  }

  @Test
  fun documentToAccount_whenExceptionOccurs_returnsNull() {
    // Arrange
    val document = Mockito.mock(DocumentSnapshot::class.java)
    `when`(document.id).thenReturn(account.uid)
    // Simulate an exception when accessing the "firstName" field
    `when`(document.getString("firstName")).thenThrow(RuntimeException("Test exception"))

    // Act
    val result = invokeDocumentToAccount(document)

    // Assert
    assertNull(result)
  }

  @Test
  fun documentToAccount_whenExtraFieldsPresent_returnsAccount() {
    // Arrange
    val document = Mockito.mock(DocumentSnapshot::class.java)
    `when`(document.id).thenReturn(account.uid)
    `when`(document.getString("firstName")).thenReturn(account.firstName)
    `when`(document.getString("lastName")).thenReturn(account.lastName)
    `when`(document.getString("email")).thenReturn(account.email)
    `when`(document.getTimestamp("birthDate")).thenReturn(account.birthDate)
    // Extra field that is not used by the repository
    `when`(document.getString("isWorker")).thenReturn("false")
    `when`(document.getString("profilePicture")).thenReturn(account.profilePicture)

    // Act
    val result = invokeDocumentToAccount(document)

    // Assert
    assertNotNull(result)
    assertEquals(account, result)
  }

  // ----- Helper Method for Testing Private Method -----

  /** Uses reflection to invoke the private `documentToAccount` method. */
  private fun invokeDocumentToAccount(document: DocumentSnapshot): Account? {
    val method =
        AccountRepositoryFirestore::class
            .java
            .getDeclaredMethod("documentToAccount", DocumentSnapshot::class.java)
    method.isAccessible = true
    return method.invoke(accountRepositoryFirestore, document) as Account?
  }

  // ----- Init Method Tests -----

  @Test
  fun init_whenCurrentUserNotNull_callsOnSuccess() {
    val authStateListenerCaptor = argumentCaptor<FirebaseAuth.AuthStateListener>()
    val mockFirebaseUser = Mockito.mock(FirebaseUser::class.java)

    doNothing().`when`(mockFirebaseAuth).addAuthStateListener(authStateListenerCaptor.capture())
    `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

    var callbackCalled = false

    accountRepositoryFirestore.init(onSuccess = { callbackCalled = true })

    // Simulate auth state change
    authStateListenerCaptor.firstValue.onAuthStateChanged(mockFirebaseAuth)

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(callbackCalled)
  }

  @Test
  fun init_whenCurrentUserIsNull_doesNotCallOnSuccess() {
    val authStateListenerCaptor = argumentCaptor<FirebaseAuth.AuthStateListener>()

    doNothing().`when`(mockFirebaseAuth).addAuthStateListener(authStateListenerCaptor.capture())
    `when`(mockFirebaseAuth.currentUser).thenReturn(null)

    var callbackCalled = false

    accountRepositoryFirestore.init(onSuccess = { callbackCalled = true })

    // Simulate auth state change
    authStateListenerCaptor.firstValue.onAuthStateChanged(mockFirebaseAuth)

    shadowOf(Looper.getMainLooper()).idle()

    assertFalse(callbackCalled)
  }

  @Test
  fun fetchAccountProfileImageAsBitmap_emptyUrl_returnsDefaultBitmap() {
    val accountId = "someAccountId"
    val documentId = "profilePicture"

    // Mock Firestore document get
    val tcs = TaskCompletionSource<DocumentSnapshot>()
    `when`(mockCollectionReference.document(accountId)).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.get()).thenReturn(tcs.task)
    `when`(mockDocumentSnapshot.exists()).thenReturn(true)
    `when`(mockDocumentSnapshot.get(documentId)).thenReturn("")
    tcs.setResult(mockDocumentSnapshot)

    var onSuccessCalled = false
    var returnedBitmap: Bitmap? = null

    accountRepositoryFirestore.fetchAccountProfileImageAsBitmap(
        profilePictureUrl = accountId,
        onSuccess = {
          onSuccessCalled = true
          returnedBitmap = it
        },
        onFailure = { fail("Should not fail with empty URL") })

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(onSuccessCalled)
    assertNotNull(returnedBitmap)
    // On ne vérifie pas exactement le bitmap, mais on s'assure qu'il n'est pas null
  }

  @Test
  fun fetchAccountProfileImageAsBitmap_firestoreFails_callsOnFailure() {
    val accountId = "someAccountId"
    val documentId = "profilePicture"
    val firestoreException = Exception("Firestore error")

    val tcsDoc = TaskCompletionSource<DocumentSnapshot>()
    `when`(mockCollectionReference.document(accountId)).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.get()).thenReturn(tcsDoc.task)

    var onFailureCalled = false

    accountRepositoryFirestore.fetchAccountProfileImageAsBitmap(
        profilePictureUrl = accountId,
        onSuccess = { fail("Should not succeed") },
        onFailure = {
          onFailureCalled = true
          assertEquals(firestoreException.message, it.message)
        })

    // Simuler une erreur Firestore
    tcsDoc.setException(firestoreException)

    shadowOf(Looper.getMainLooper()).idle()

    assertTrue(onFailureCalled)
  }
}
