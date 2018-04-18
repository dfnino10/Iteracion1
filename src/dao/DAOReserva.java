package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import vos.Cliente;
import vos.Espacio;
import vos.Reserva;

public class DAOReserva {
	private ArrayList<Object> recursos;

	private Connection conn;

	public DAOReserva() {
		recursos = new ArrayList<Object>();
	}

	public void cerrarRecursos() {
		for (Object ob : recursos) {
			if (ob instanceof PreparedStatement) {
				try {
					((PreparedStatement) ob).close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		}
	}

	public void setConn(Connection con) {
		this.conn = con;
	}

	public ArrayList<Reserva> darReservas() throws SQLException, Exception {
		ArrayList<Reserva> reservas = new ArrayList<Reserva>();

		String sql = "SELECT * FROM RESERVAS";

		PreparedStatement prepStmt = conn.prepareStatement(sql);
		recursos.add(prepStmt);
		ResultSet rs = prepStmt.executeQuery();

		while (rs.next()) {
			long idCliente = Long.parseLong(rs.getString("IDCLIENTE"));
			long idEspacio = Long.parseLong(rs.getString("IDESPACIO"));
			Date fechaInicio = Date.valueOf(rs.getString("FECHAINICIO"));
			int duracion = Integer.parseInt(rs.getString("DURACION"));
			Date fechaReserva = Date.valueOf(rs.getString("FECHARESERVA"));
			double precio = Double.parseDouble(rs.getString("PRECIO"));
			boolean cancelado = false;
			if (rs.getString("CANCELADO").equals('Y')) {
				cancelado = true;
			}
			reservas.add(new Reserva(idCliente, idEspacio, fechaInicio, duracion, fechaReserva, cancelado, precio));
		}
		return reservas;
	}

	public void addReserva(Reserva reserva) throws SQLException, Exception
	{
		String sql = "INSERT INTO RESERVAS (idCliente, idEspacio, duracion, fechaInicio, fechaReserva, precio, cancelado) VALUES (";
		sql += reserva.getIdCliente() + ",";
		sql += reserva.getIdEspacio() + ",";
		sql += reserva.getDuracion() + ",";
		sql += "TO_DATE('"+reserva.getFechaInicio().getDay() + "-" + reserva.getFechaInicio().getMonth() +"-" +reserva.getFechaInicio().getYear()   + "','DD-MM-YYYY'),";
		sql += "TO_DATE('"+reserva.getFechaReserva().getDay() + "-" + reserva.getFechaReserva().getMonth() +"-" +reserva.getFechaReserva().getYear()   + "','DD-MM-YYYY'),";

		DAOCliente daoCliente = new DAOCliente();
		DAOEspacio daoEspacio = new DAOEspacio();
		daoCliente.setConn(conn);
		daoEspacio.setConn(conn);

		Cliente cliente = daoCliente.buscarCliente(reserva.getIdCliente());
		Espacio espacio = daoEspacio.buscarEspacio(reserva.getIdEspacio());

		if (!reserva.isCancelado()) {
			reserva.setPrecio(espacio.getPrecio() * reserva.getDuracion());
		}

		char cancelado = 'N';
		if (reserva.isCancelado()) {
			cancelado = 'Y';
		}
		sql += reserva.getPrecio() + ",";
		sql += cancelado + ")";
		

		cliente.getReservas().add(reserva.getIdEspacio());
		espacio.getReservas().add(reserva.getIdCliente());
		daoCliente.updateCliente(cliente);
		daoEspacio.updateEspacio(espacio);

		System.out.println("SQL stmt:" + sql);

		PreparedStatement prepStmt = conn.prepareStatement(sql);
		recursos.add(prepStmt);
		prepStmt.executeQuery();
	}

	public void updateReserva(Reserva reserva) throws SQLException, Exception {
		String sql = "UPDATE RESERVAS SET ";
		sql += "duracion = " + reserva.getDuracion() + ",";
		sql += "fechaInicio = TO_DATE('"+reserva.getFechaInicio().getDay() + "-" + reserva.getFechaInicio().getMonth() +"-" +reserva.getFechaInicio().getYear()   + "','DD-MM-YYYY'),";
		sql += "fechaReserva = TO_DATE('"+reserva.getFechaReserva().getDay() + "-" + reserva.getFechaReserva().getMonth() +"-" +reserva.getFechaReserva().getYear()   + "','DD-MM-YYYY'),";

		DAOCliente daoCliente = new DAOCliente();
		DAOEspacio daoEspacio = new DAOEspacio();
		daoCliente.setConn(conn);
		daoEspacio.setConn(conn);

		Cliente cliente = daoCliente.buscarCliente(reserva.getIdCliente());
		Espacio espacio = daoEspacio.buscarEspacio(reserva.getIdEspacio());

		if (!reserva.isCancelado()) {
			reserva.setPrecio(espacio.getPrecio() * reserva.getDuracion());
		}

		char cancelado = 'N';
		if (reserva.isCancelado()) {
			cancelado = 'Y';
		}
		sql += "precio = " + reserva.getPrecio() + ",";
		sql += "cancelado = " + cancelado;
		sql += " WHERE IDCLIENTE = " + reserva.getIdCliente() + " AND IDESPACIO = " + reserva.getIdEspacio();

		System.out.println("SQL stmt:" + sql);

		PreparedStatement prepStmt = conn.prepareStatement(sql);
		recursos.add(prepStmt);
		prepStmt.executeQuery();
	}

	public void deleteReserva(Reserva reserva) throws SQLException, Exception {
		String sql = "DELETE FROM RESERVAS";
		sql += " WHERE IDCLIENTE = " + reserva.getIdCliente() + " AND IDESPACIO = " + reserva.getIdEspacio();

		System.out.println("SQL stmt:" + sql);

		PreparedStatement prepStmt = conn.prepareStatement(sql);
		recursos.add(prepStmt);
		prepStmt.executeQuery();
	}

	public Reserva buscarReserva(long idCliente, long idEspacio) throws SQLException, Exception {
		String sql = "SELECT * FROM RESERVAS WHERE IDCLIENTE = " + idCliente + " AND IDESPACIO = " + idEspacio;

		System.out.println("SQL stmt:" + sql);

		PreparedStatement prepStmt = conn.prepareStatement(sql);
		recursos.add(prepStmt);
		ResultSet rs = prepStmt.executeQuery();
		
		if(!rs.next())
		{
			throw new Exception ("No se encontr� ninguna reserva con el idCliente = "+idCliente + " y con idEspacio = " + idEspacio);
		}
		
		Date fechaInicio = Date.valueOf(rs.getString("FECHAINICIO"));
		int duracion = Integer.parseInt(rs.getString("DURACION"));
		Date fechaReserva = Date.valueOf(rs.getString("FECHARESERVA"));
		boolean cancelado = false;
		double precio = Double.parseDouble(rs.getString("PRECIO"));
		if (rs.getString("CANCELADO").equals('Y')) {
			cancelado = true;
		}

		return new Reserva(idCliente, idEspacio, fechaInicio, duracion, fechaReserva, cancelado, precio);
	}

	public ArrayList<Long> buscarReservasIdCliente(long idCliente) throws SQLException, Exception {

		ArrayList<Long> reservas = new ArrayList<Long>();

		String sql = "SELECT * FROM RESERVAS WHERE IDCLIENTE = " + idCliente;

		System.out.println("SQL stmt:" + sql);

		PreparedStatement prepStmt = conn.prepareStatement(sql);
		recursos.add(prepStmt);
		ResultSet rs = prepStmt.executeQuery();

		while (rs.next()) {
			long idEspacio = Long.parseLong(rs.getString("IDESPACIO"));
			reservas.add(idEspacio);
		}
		return reservas;
	}

	public ArrayList<Long> buscarReservasIdEspacio(long idEspacio) throws SQLException, Exception {

		ArrayList<Long> reservas = new ArrayList<Long>();

		String sql = "SELECT * FROM RESERVAS WHERE IDESPACIO = " + idEspacio;

		System.out.println("SQL stmt:" + sql);

		PreparedStatement prepStmt = conn.prepareStatement(sql);
		recursos.add(prepStmt);
		ResultSet rs = prepStmt.executeQuery();

		while (rs.next()) {
			long idCliente = Long.parseLong(rs.getString("IDCLIENTE"));

			reservas.add(idCliente);
		}
		return reservas;
	}
}
