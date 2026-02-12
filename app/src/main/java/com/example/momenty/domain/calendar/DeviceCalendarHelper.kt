package com.example.momenty.domain.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.TimeZone

class DeviceCalendarHelper(private val context: Context) {
    data class DeviceCalendar(
        val id: String,
        val name: String,
        val accountName: String,
        val color: Int
    )

    /**
     * 디바이스의 모든 캘린더 조회
     */
    suspend fun getAvailableCalendars(): List<DeviceCalendar> = withContext(Dispatchers.IO) {
        val calendars = mutableListOf<DeviceCalendar>()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR
        )

        try {
            val cursor: Cursor? = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null, null, null
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getString(0)
                    val name = it.getString(1)
                    val accountName = it.getString(2)
                    val color = it.getInt(3)

                    calendars.add(DeviceCalendar(id, name, accountName, color))
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        calendars
    }

    /**
     * 특정 기간의 일정 조회
     */
    suspend fun getEventsInRange(startDate: Long, endDate: Long): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val events = mutableListOf<CalendarEvent>()
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.EVENT_COLOR
        )

        val selection = "(${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?)"
        val selectionArgs = arrayOf(startDate.toString(), endDate.toString())

        try {
            val cursor: Cursor? = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Events.DTSTART} ASC"
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getString(0)
                    val title = it.getString(1) ?: "제목 없음"
                    val startTime = it.getLong(2)
                    val calendarId = it.getString(5)
                    events.add(
                        CalendarEvent(
                            id = id,
                            petId = null, // 추후 메타데이터로 저장 가능
                            calendarId = calendarId,
                            title = title,
                            scheduleDate = Date(startTime), // 수정: scheduleDate 대신 startTime 사용
                            alarmTime = "", // 기본값 (알림은 별도 처리 필요)
                            petName = null,
                            type = "DEVICE" // 디바이스 캘린더 이벤트 타입
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        events
    }

    /**
     * 일정 추가
     */
    suspend fun addEvent(
        calendarId: String,
        title: String,
        startTime: Date,
        endTime: Date,
        description: String? = null,
        location: String? = null
    ): Long? = withContext(Dispatchers.IO){
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DTSTART, startTime.time)
            put(CalendarContract.Events.DTEND, endTime.time)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            description?.let { put(CalendarContract.Events.DESCRIPTION, it) }
            location?.let { put(CalendarContract.Events.EVENT_LOCATION, it) }
        }

        try {
            val uri = context.contentResolver.insert(
                CalendarContract.Events.CONTENT_URI,
                values
            )
            uri?.let { ContentUris.parseId(it) }
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 일정 수정
     */
    suspend fun updateEvent(
        eventId: String,
        title: String? = null,
        startTime: Date? = null,
        endTime: Date? = null,
        description: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            title?.let { put(CalendarContract.Events.TITLE, it) }
            startTime?.let { put(CalendarContract.Events.DTSTART, it.time) }
            endTime?.let { put(CalendarContract.Events.DTEND, it.time) }
            description?.let { put(CalendarContract.Events.DESCRIPTION, it) }
        }

        try {
            val uri = ContentUris.withAppendedId(
                CalendarContract.Events.CONTENT_URI,
                eventId.toLong()
            )
            val rowsUpdated = context.contentResolver.update(uri, values, null, null)
            rowsUpdated > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 일정 삭제
     */
    suspend fun deleteEvent(eventId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = ContentUris.withAppendedId(
                CalendarContract.Events.CONTENT_URI,
                eventId.toLong()
            )
            val rowsDeleted = context.contentResolver.delete(uri, null, null)
            rowsDeleted > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}