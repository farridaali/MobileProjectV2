package com.example.mobileprojectv2

import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class GroceryDaoTest {

    @Mock
    lateinit var groceryDao: GroceryDao

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testInsertItem() {
        runBlocking {
            val item = ItemEntity(1, "Apple", 5, 1.5, false)

            groceryDao.insertItem(item)

            verify(groceryDao).insertItem(item)
        }
    }

    @Test
    fun testUpdateItem() {
        runBlocking {
            val item = ItemEntity(1, "Apple", 5, 1.5, false)

            groceryDao.updateItem(item)

            verify(groceryDao).updateItem(item)
        }
    }

    @Test
    fun testDeleteItem() {
        runBlocking {
            val item = ItemEntity(1, "Apple", 5, 1.5, false)

            groceryDao.deleteItem(item)

            verify(groceryDao).deleteItem(item)
        }
    }

    @Test
    fun testGetAllItems() {
        runBlocking {
            val items = listOf(
                ItemEntity(1, "Apple", 5, 1.5, false),
                ItemEntity(2, "Banana", 3, 0.5, true)
            )

            `when`(groceryDao.getAllItems()).thenReturn(items)

            val result = groceryDao.getAllItems()

            assertEquals(items, result)
            verify(groceryDao).getAllItems()
        }
    }

    @Test
    fun testSetBoughtStatus() {
        runBlocking {
            val id = 1
            val state = true

            groceryDao.setBoughtStatus(id, state)

            verify(groceryDao).setBoughtStatus(id, state)
        }
    }

    @Test
    fun testGetTotalCost() {
        runBlocking {
            val totalCost = 20.0

            `when`(groceryDao.getTotalCost()).thenReturn(totalCost)

            val result = groceryDao.getTotalCost()

            assertEquals(totalCost, result !! , 0.001)
            verify(groceryDao).getTotalCost()
        }
    }

    @Test
    fun testGetItemById() {
        runBlocking {
            val item = ItemEntity(1, "Apple", 5, 1.5, false)

            `when`(groceryDao.getItemById(1)).thenReturn(item)

            val result = groceryDao.getItemById(1)

            assertNotNull(result)
            assertEquals(item, result)
            verify(groceryDao).getItemById(1)
        }
    }
}
