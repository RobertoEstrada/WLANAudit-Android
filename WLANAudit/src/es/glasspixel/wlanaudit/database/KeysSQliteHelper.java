package es.glasspixel.wlanaudit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class KeysSQliteHelper extends SQLiteOpenHelper {

	String sqlCreate = "CREATE TABLE Keys (nombre TEXT not null , key TEXT not null , PRIMARY KEY (nombre, key))";

	public KeysSQliteHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(sqlCreate);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		 //Se elimina la versión anterior de la tabla
        db.execSQL("DROP TABLE IF EXISTS Keys");
 
        //Se crea la nueva versión de la tabla
        db.execSQL(sqlCreate);

	}

}
