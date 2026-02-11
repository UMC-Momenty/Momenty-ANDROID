package com.example.momenty.domain.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
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
    fun getAvailableCalendars(): List<DeviceCalendar>   {
        val calendars = mutableListOf<DeviceCalendar>()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR
        )

        try{
            val cursor: Cursor? = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null, null, null
            )
            cursor?.use{
                while(it.moveToNext()){
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

        return calendars
    }

    /**
     * 특정 기간의 일정 조회
     */
    fun getEventsInRange(startDate: Long, endDate: Long): List<CalendarEvent> {
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
                    val endTime = it.getLong(3)
                    val description = if (!it.isNull(4)) it.getString(4) else null
                    val calendarId = it.getString(5)
                    val color = if (!it.isNull(6)) it.getInt(6) else null

                    events.add(
                        CalendarEvent(
                            id = id,
                            title = title,
                            startTime = Date(startTime),
                            endTime = Date(endTime),
                            description = description,
                            calendarId = calendarId,
                            color = color,
                            petId = null, // 추후 메타데이터로 저장 가능
                            petName = null
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        return events
    }

    /**
     * 일정 추가
     */
    fun addEvent(
        calendarId: String,
        title: String,
        startTime: Date,
        endTime: Date,
        description: String? = null,
        location: String? = null
    ): Long? {
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DTSTART, startTime.time)
            put(CalendarContract.Events.DTEND, endTime.time)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            description?.let { put(CalendarContract.Events.DESCRIPTION, it) }
            location?.let { put(CalendarContract.Events.EVENT_LOCATION, it) }
        }

        return try {
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
    fun updateEvent(
        eventId: String,
        title: String? = null,
        startTime: Date? = null,
        endTime: Date? = null,
        description: String? = null
    ): Boolean {
        val values = ContentValues().apply {
            title?.let { put(CalendarContract.Events.TITLE, it) }
            startTime?.let { put(CalendarContract.Events.DTSTART, it.time) }
            endTime?.let { put(CalendarContract.Events.DTEND, it.time) }
            description?.let { put(CalendarContract.Events.DESCRIPTION, it) }
        }

        return try {
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
    fun deleteEvent(eventId: String): Boolean {
        return try {
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