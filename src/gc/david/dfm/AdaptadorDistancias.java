package gc.david.dfm;

import gc.david.dfm.db.Distance;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Sets an adapter for distance entries from the database to show to the user to
 * choose a distance.
 * 
 * @author David
 * 
 */
public class AdaptadorDistancias extends ArrayAdapter<Distance> {
	
	private Activity context;
	private List<Distance> datos;
	
	public AdaptadorDistancias(Activity context, List<Distance> datos) {
		super(context, R.layout.list_item, datos);
		this.context = context;
		this.datos = datos;
	}

	public List<Distance> getDatos() {
		return datos;
	}
	
	static class ViewHolder {
		TextView titulo;
		TextView distancia;
		TextView fecha;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View item = convertView;
		ViewHolder holder;
		
		if (item == null){
			LayoutInflater inflater = this.context.getLayoutInflater();
			item = inflater.inflate(R.layout.list_item, null);
			
			holder = new ViewHolder();
			holder.titulo = (TextView) item.findViewById(R.id.alias);
			holder.distancia = (TextView) item.findViewById(R.id.distancia);
			holder.fecha = (TextView) item.findViewById(R.id.fecha);
			
			item.setTag(holder);
		} else {
			holder = (ViewHolder) item.getTag();
		}
		
		holder.titulo.setText(datos.get(position).getNombre().toString());
		holder.distancia.setText(datos.get(position).getDistancia().toString());
		holder.fecha.setText(datos.get(position).getFecha().toString());
		
		return item;
	}
}