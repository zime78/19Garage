package com.zime.garage.viewmodel

import com.zime.garage.common.LocalFileManager
import com.zime.garage.db.viewModel.ClassificationModel
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClassificationViewModelTest {

    private val tmp1 = "_test_"
    private val tmp2 = "_test_updated_"

    @BeforeAll
    fun init() {
        // Ensure base files exist
        LocalFileManager.load()
    }

    @BeforeEach
    fun cleanBefore() {
        // Ensure a clean slate for test names
        val model = ClassificationModel()
        model.deleteClassificationType(tmp1)
        model.deleteClassificationType(tmp2)
    }

    @AfterEach
    fun cleanAfter() {
        // Cleanup any leftovers
        val model = ClassificationModel()
        model.deleteClassificationType(tmp1)
        model.deleteClassificationType(tmp2)
    }

    @Test
    fun addUpdateDeleteFlow() {
        val vm = ClassificationViewModel()
        vm.load()
        val originalCount = vm.classifications.size

        // Add
        val added = vm.add(tmp1)
        assertTrue(added, "분류 추가에 실패했습니다")
        assertTrue(vm.classifications.contains(tmp1), "추가 후 상태에 항목이 반영되어야 합니다")

        // Update
        val updated = vm.update(tmp1, tmp2)
        assertTrue(updated, "분류 수정에 실패했습니다")
        assertFalse(vm.classifications.contains(tmp1), "수정 전 항목이 리스트에 남아있지 않아야 합니다")
        assertTrue(vm.classifications.contains(tmp2), "수정 후 항목이 리스트에 존재해야 합니다")

        // Delete
        val deleted = vm.delete(tmp2)
        assertTrue(deleted, "분류 삭제에 실패했습니다")
        assertFalse(vm.classifications.contains(tmp2), "삭제된 항목이 리스트에 남아있지 않아야 합니다")

        // size should be back to original (since we added then removed)
        vm.refresh()
        assertEquals(originalCount, vm.classifications.size, "작업 전과 동일한 개수여야 합니다")
    }
}
