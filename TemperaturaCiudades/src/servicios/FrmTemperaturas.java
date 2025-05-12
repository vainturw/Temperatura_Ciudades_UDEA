package servicios;

import entidades.RegistroTemperatura;
import datechooser.beans.DateChooserCombo;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class FrmTemperaturas extends JFrame {

    private DateChooserCombo txtDesde;
    private DateChooserCombo txtHasta;
    private JButton btnGraficar;
    private JPanel pnlGrafico;

    private DateChooserCombo txtFecha;
    private JButton btnEstadisticas;
    private JTextArea txtResultado;

    private List<RegistroTemperatura> datos;

    public FrmTemperaturas() {
        setTitle("Análisis de Temperaturas");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        datos = TemperaturaServicio.cargarDatos("datos/Temperaturas.csv");

        add(crearPanelConPestanas(), BorderLayout.CENTER);
        setVisible(true);
    }

    private JTabbedPane crearPanelConPestanas() {
        JTabbedPane pestañas = new JTabbedPane();
        pestañas.addTab("Gráfico", crearPanelGrafico());
        pestañas.addTab("Estadísticas", crearPanelEstadisticas());
        return pestañas;
    }

    private JPanel crearPanelGrafico() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(new TitledBorder("Promedio por ciudad"));

        JPanel pnlSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSuperior.add(new JLabel("Desde:"));
        txtDesde = new DateChooserCombo();
        txtDesde.setPreferredSize(new Dimension(120, 25));
        pnlSuperior.add(txtDesde);

        pnlSuperior.add(new JLabel("Hasta:"));
        txtHasta = new DateChooserCombo();
        txtHasta.setPreferredSize(new Dimension(120, 25));
        pnlSuperior.add(txtHasta);

        ImageIcon iconoGrafico = new ImageIcon("iconos/grafico.png");
        Image imgGrafico = iconoGrafico.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        btnGraficar = new JButton(new ImageIcon(imgGrafico));
        btnGraficar.setPreferredSize(new Dimension(40, 40));
        btnGraficar.setToolTipText("Mostrar gráfico de promedio por ciudad");
        pnlSuperior.add(btnGraficar);

        pnlGrafico = new JPanel(new BorderLayout());

        pnl.add(pnlSuperior, BorderLayout.NORTH);
        pnl.add(pnlGrafico, BorderLayout.CENTER);

        btnGraficar.addActionListener(e -> graficar());

        return pnl;
    }

    private void graficar() {
        try {
            LocalDate desde = convertirFecha(txtDesde);
            LocalDate hasta = convertirFecha(txtHasta);

            List<RegistroTemperatura> filtrados = TemperaturaServicio.filtrar(desde, hasta, datos);
            Par<List<String>, List<Double>> par = TemperaturaServicio.extraerPromediosPorCiudad(filtrados);

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            List<String> ciudades = par.getX();
            List<Double> promedios = par.getY();

            for (int i = 0; i < ciudades.size(); i++) {
                dataset.addValue(promedios.get(i), "Temperatura", ciudades.get(i));
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "Promedio por ciudad", "Ciudad", "Temperatura", dataset
            );

            pnlGrafico.removeAll();
            pnlGrafico.add(new ChartPanel(chart), BorderLayout.CENTER);
            pnlGrafico.revalidate();
            pnlGrafico.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fechas inválidas.");
        }
    }

    private JPanel crearPanelEstadisticas() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(new TitledBorder("Ciudad más y menos calurosa"));

        JPanel pnlSuperior = new JPanel();
        pnlSuperior.add(new JLabel("Fecha:"));
        txtFecha = new DateChooserCombo();
        txtFecha.setPreferredSize(new Dimension(120, 25));
        pnlSuperior.add(txtFecha);

        ImageIcon iconoEstadisticas = new ImageIcon("iconos/estadistica.png");
        Image imgEstadisticas = iconoEstadisticas.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        btnEstadisticas = new JButton(new ImageIcon(imgEstadisticas));
        btnEstadisticas.setPreferredSize(new Dimension(40, 40));
        btnEstadisticas.setToolTipText("Mostrar ciudad más y menos calurosa");
        pnlSuperior.add(btnEstadisticas);

        txtResultado = new JTextArea(10, 30);
        txtResultado.setEditable(false);
        JScrollPane scroll = new JScrollPane(txtResultado);

        pnl.add(pnlSuperior, BorderLayout.NORTH);
        pnl.add(scroll, BorderLayout.CENTER);

        btnEstadisticas.addActionListener(e -> mostrarEstadisticas());

        return pnl;
    }

    private void mostrarEstadisticas() {
        try {
            LocalDate fecha = convertirFecha(txtFecha);
            Map<String, String> resultado = TemperaturaServicio.getResumen(fecha, datos);

            txtResultado.setText("");
            resultado.forEach((clave, valor) ->
                    txtResultado.append(clave + ": " + valor + "\n")
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fecha inválida");
        }
    }

    private LocalDate convertirFecha(DateChooserCombo chooser) {
        return LocalDate.of(
                chooser.getSelectedDate().get(Calendar.YEAR),
                chooser.getSelectedDate().get(Calendar.MONTH) + 1,
                chooser.getSelectedDate().get(Calendar.DAY_OF_MONTH)
        );
    }

    public static void main(String[] args) {
        new FrmTemperaturas();
    }
}

