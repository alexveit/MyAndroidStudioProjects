package alex.veit.bookreadingstats;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BookStatsDatabaseManager {

	private class CustomSQLiteOpenHelper extends SQLiteOpenHelper {

		public CustomSQLiteOpenHelper(final Context context, final String dbName) {
			super(context, dbName, null, DB_VERSION);
		}

		// TODO: override the constructor and other methods for the parent class

		@Override
		public void onCreate(final SQLiteDatabase db) {
			// the SQLite query string that will create our 3 column database
			// table.
			String newTableQueryString = "create table " + TABLE_NAME + " ("
					+ TABLE_ROW_ID
					+ " integer primary key autoincrement not null,"
					+ TABLE_ROW_NAME + " text," + TABLE_ROW_TOTAL_PAGES
					+ " integer," + TABLE_ROW_LASTSES + " integer" + ");";

			// execute the query string to the database.
			db.execSQL(newTableQueryString);

			newTableQueryString = "create table " + TABLE_NAME_TWO + " ("
					+ TABLE_ROW_ID
					+ " integer primary key autoincrement not null,"
					+ TABLE_ROW_ID_REF + " integer," + TABLE_ROW_SESSIONS
					+ " text," + TABLE_ROW_HOURS + " integer,"
					+ TABLE_ROW_MINUTES + " integer," + TABLE_ROW_START
					+ " text," + TABLE_ROW_END + " text," + TABLE_ROW_SES_READ
					+ " integer" + ");";

			db.execSQL(newTableQueryString);

		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
				final int newVersion) {
			/*
			 * db.execSQL("ALTER TABLE book_sessions ADD COLUMN start TEXT");
			 * db.execSQL("ALTER TABLE book_sessions ADD COLUMN end TEXT");
			 */
			// db.execSQL("ALTER TABLE book_sessions ADD COLUMN ses_pages_read integer DEFAULT 0");
			// db.execSQL("ALTER TABLE book_stats DROP COLUMN pages_red");

			/*final ContentValues values = new ContentValues();
			values.put(TABLE_ROW_SES_READ, 36);
			db.update(TABLE_NAME_TWO, values, TABLE_ROW_ID + "=" + 179, null);*/

		}
	}

	private static final String DB_NAME = "BookStatsDB"; // the name of our
	private static final int DB_VERSION = 1; // the version of the database
	private static final String TABLE_NAME = "book_stats";
	private static final String TABLE_NAME_TWO = "book_sessions";
	private static final String TABLE_ROW_HOURS = "hours";
	private static final String TABLE_ROW_ID = "id";
	private static final String TABLE_ROW_ID_REF = "bookID";
	private static final String TABLE_ROW_MINUTES = "minutes";
	private static final String TABLE_ROW_LASTSES = "last_ses";
	private static final String TABLE_ROW_NAME = "name";
	private static final String TABLE_ROW_SESSIONS = "sessions";
	private static final String TABLE_ROW_TOTAL_PAGES = "total_pages";
	private static final String TABLE_ROW_START = "start";
	private static final String TABLE_ROW_END = "end";
	private static final String TABLE_ROW_SES_READ = "ses_pages_read";

	public static String getName() {
		return DB_NAME;
	}

	SQLiteDatabase _db; // a reference to the database manager class.

	Context context;

	public BookStatsDatabaseManager(final Context context, final String dbName) {
		this.context = context;
		final CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(
				context, dbName);
		_db = helper.getWritableDatabase();
		// helper.onUpgrade(_db, 0, 0);
	}

	public void addBookRow(final BookStats bs) {
		// this is a key value pair holder used by android's SQLite functions
		final ContentValues valuesBook = new ContentValues();

		// this is how you add a value to a ContentValues object
		// we are passing in a key string and a value string each time
		valuesBook.put(TABLE_ROW_NAME, bs._name);
		valuesBook.put(TABLE_ROW_TOTAL_PAGES, bs._totalPages);
		// valuesBook.put(TABLE_ROW_PAGES_RED, bs._pagesRed);
		valuesBook.put(TABLE_ROW_LASTSES, bs._lastSession);

		// ask the database object to insert the new data
		try {
			_db.insert(TABLE_NAME, null, valuesBook);
			final String query = "SELECT " + TABLE_ROW_ID + " from "
					+ TABLE_NAME + " order by " + TABLE_ROW_ID
					+ " DESC limit 1";
			final Cursor c = _db.rawQuery(query, null);
			if ((c != null) && c.moveToFirst()) {
				bs._id = c.getLong(0); // The 0 is the column index, we only
				// have 1 column, so the index is 0
			}
		} catch (final Exception e) {
			Log.e("DB ERROR", e.toString()); // prints the error message to the
			// log
			e.printStackTrace(); // prints the stack trace to the log
		}
	}

	public void addSessions(final BookStats bs) {

		for (int i = 0; i < bs._sessions.size(); i++) {
			if (bs._sessions.get(i)._id == -1) {
				final ContentValues valuesSessions = new ContentValues();
				valuesSessions.put(TABLE_ROW_SESSIONS,
						bs._sessions.get(i)._value);
				valuesSessions.put(TABLE_ROW_ID_REF, bs._id);
				valuesSessions.put(TABLE_ROW_HOURS, bs._sessions.get(i)._hours);
				valuesSessions.put(TABLE_ROW_MINUTES,
						bs._sessions.get(i)._minutes);
				valuesSessions.put(TABLE_ROW_START, bs._sessions.get(i)
						.getStartDB());
				valuesSessions.put(TABLE_ROW_END, bs._sessions.get(i)
						.getEndDB());
				valuesSessions.put(TABLE_ROW_SES_READ,
						bs._sessions.get(i)._pagesRead);
				_db.insert(TABLE_NAME_TWO, null, valuesSessions);

				try {
					final String query = "SELECT " + TABLE_ROW_ID + " from "
							+ TABLE_NAME_TWO + " order by " + TABLE_ROW_ID
							+ " DESC limit 1";
					final Cursor c = _db.rawQuery(query, null);
					if ((c != null) && c.moveToFirst()) {
						bs._sessions.get(i)._id = (int) c.getLong(0); // The 0
																		// is
																		// the
																		// column
																		// index,
																		// we
																		// only
						// have 1 column, so the index is 0
					}
				} catch (final Exception e) {
					Log.e("DB ERROR", e.toString()); // prints the error message
														// to the
					// log
					e.printStackTrace(); // prints the stack trace to the log
				}
			}
		}
		bs._hasNewSessions = false;
	}

	public void close() {
		_db.close();
	}

	public void deleteRow(final int rowID) {
		// ask the database manager to delete the row of given id
		try {
			_db.delete(TABLE_NAME, TABLE_ROW_ID + "=" + rowID, null);
			_db.delete(TABLE_NAME_TWO, TABLE_ROW_ID_REF + "=" + rowID, null);
		} catch (final Exception e) {
			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}
	}

	public void deleteSession(final Session session) {
		try {
			_db.delete(TABLE_NAME_TWO, TABLE_ROW_ID + "=" + session._id, null);
		} catch (final Exception e) {
			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}

	}

	public ArrayList<BookStats> getAllBooks() {

		final ArrayList<BookStats> bsArray = new ArrayList<BookStats>();

		final Cursor cursorBook = _db.query(TABLE_NAME, new String[]{
				TABLE_ROW_ID, TABLE_ROW_NAME, TABLE_ROW_TOTAL_PAGES,
				TABLE_ROW_LASTSES}, null, null, null, null, null);

		cursorBook.moveToFirst();

		if (!cursorBook.isAfterLast()) {
			do {

				final BookStats bs = new BookStats();
				bs._id = cursorBook.getLong(0);
				bs._name = cursorBook.getString(1);
				bs._totalPages = cursorBook.getInt(2);
				bs._lastSession = cursorBook.getInt(3);

				final Cursor cursorSessions = _db.query(TABLE_NAME_TWO,
						new String[]{TABLE_ROW_ID, TABLE_ROW_ID_REF,
								TABLE_ROW_SESSIONS, TABLE_ROW_HOURS,
								TABLE_ROW_MINUTES, TABLE_ROW_START,
								TABLE_ROW_END, TABLE_ROW_SES_READ},
						TABLE_ROW_ID_REF + "='" + bs._id + "'", null, null,
						null, null);
				cursorSessions.moveToFirst();
				if (!cursorSessions.isAfterLast()) {
					final ArrayList<Session> sessions = new ArrayList<Session>();
					do {
						final Session ses = new Session(
								cursorSessions.getInt(0),
								cursorSessions.getString(2),
								cursorSessions.getInt(3),
								cursorSessions.getInt(4),
								cursorSessions.getInt(7));
						ses.setStartFromDB(cursorSessions.getString(5), bs);
						ses.setEndFromDB(cursorSessions.getString(6));
						sessions.add(ses);
					} while (cursorSessions.moveToNext());
					bs._sessions.addAll(sessions);
				}

				bsArray.add(bs);
			}
			// move the cursor's pointer up one position.
			while (cursorBook.moveToNext());
		}

		return bsArray;
	}

	public void updateBookRow(final BookStats bookStats) {

		// this is a key value pair holder used by android's SQLite functions
		final ContentValues values = new ContentValues();
		values.put(TABLE_ROW_NAME, bookStats._name);
		values.put(TABLE_ROW_TOTAL_PAGES, bookStats._totalPages);
		// values.put(TABLE_ROW_PAGES_RED, bookStats._pagesRed);
		values.put(TABLE_ROW_LASTSES, bookStats._lastSession);

		// ask the database object to update the database row of given rowID
		try {
			_db.update(TABLE_NAME, values, TABLE_ROW_ID + "=" + bookStats._id,
					null);
		} catch (final Exception e) {
			Log.e("DB Error", e.toString());
			e.printStackTrace();
		}

	}

	public void updateSession(Session session) {
		final ContentValues values = new ContentValues();
		values.put(TABLE_ROW_SES_READ, session._pagesRead);


		// ask the database object to update the database row of given rowID
		try {
			_db.update(TABLE_NAME_TWO, values, TABLE_ROW_ID + "=" + session._id,
					null);
		} catch (final Exception e) {
			Log.e("DB Error", e.toString());
			e.printStackTrace();
		}
		
	}

}