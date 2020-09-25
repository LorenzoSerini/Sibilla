package quasylab.sibilla.gui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import quasylab.sibilla.core.ExecutionEnvironment;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.simulator.sampling.Measure;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class AreaChartController {

    @FXML
    public InteractiveController mainController;
    @FXML
    private NumberAxis Xaxis;
    @FXML
    private NumberAxis Yaxis;
    @FXML
    private AreaChart<Double, Double> areaChartView;

    private Double time;

    ObservableList<XYChart.Data<Double, Double>> myDataList = FXCollections.observableArrayList();
    ObservableList<XYChart.Series<Double, Double>> mySeriesList = FXCollections.observableArrayList();
    Map<String, XYChart.Series<Double, Double>> measuresTable;

    XYChart.Series index0 = new XYChart.Series();
    XYChart.Series index1 = new XYChart.Series();
    XYChart.Series index2 = new XYChart.Series();
    XYChart.Series index3 = new XYChart.Series();
    XYChart.Series index4 = new XYChart.Series();

    @FXML
    public void init(InteractiveController interactiveController) {
        mainController = interactiveController;
        this.measuresTable = new TreeMap<>();
        this.time = getEe().currentTime();
        Xaxis.setTickUnit(1);
        Xaxis.setLabel("Time");
        Yaxis.setTickUnit(1);
        Yaxis.setLabel("Occurrences");
        doSeriesList();
        setSeriesNames(mySeriesList);
    }


    //Devo creare tot liste di oggetti di tipo "Data" quante sono le mie occorrenze nel modello
    public ObservableList<XYChart.Data<Double, Double>> doDataList(int n){
        myDataList.addListener(new ListChangeListener<XYChart.Data<Double, Double>>() {
            @Override
            public void onChanged(Change<? extends XYChart.Data<Double, Double>> c) {


            }
        });

        return null;
    }


    //Creo tot serie quante sono le specie del mio modello
    public ObservableList<XYChart.Series<Double, Double>> doSeriesList(){
        if (getEe().getModel().measures().length != 0){
            for (String s: getEe().getModel().measures()) {
                XYChart.Series<Double, Double> current = new XYChart.Series<>();
                mySeriesList.add(current);
            }
        }
        return  mySeriesList;
    }

    //
    public void doHashmap(XYChart.Series<Double, Double> serie){
        for (int i = 0; i < getEe().getModel().measures().length; i++){
            measuresTable.put(getEe().getModel().measures()[i] , serie);
        }
        System.out.println(measuresTable);
        System.out.println(getEe().getModel().measure("#S", getEe().currentState()));
    }


    @FXML
    public void areaChartView() {
        updateSeries(mySeriesList);


        this.areaChartView.setData(mySeriesList);
    }

    private void updateSeries(ObservableList<XYChart.Series<Double, Double>> seriesList) {
            for (XYChart.Series<Double, Double> s : seriesList) {
                s.getData().add(new XYChart.Data<>(getEe().currentTime(), getEe().getModel().measure(s.getName(), getEe().currentState())));
                s.getData().remove(s.getData().size() - 1);
        }
    }

    private void setSeriesNames(ObservableList<XYChart.Series<Double, Double>> seriesList) {
        int i = 0;
        String[] measuresModel = getEe().getModel().measures();
        for (XYChart.Series<Double, Double> s: seriesList) {
            s.setName(measuresModel[i]);
            i++;
        }
    }
    /*

    @FXML
    public void areaChartView() {
        ObservableList<XYChart.Series<Double, Double>> seriesList = FXCollections.observableArrayList();
        Xaxis.setTickUnit(1);
        Xaxis.setLabel("Steps");

        Yaxis.setTickUnit(1);
        Yaxis.setLabel("Occurrences");

        index0.setName("Suscettible");
        index1.setName("Asintomatic");
        index2.setName("Grave");
        index3.setName("Recovered");
        index4.setName("Death");

        index0.getData().add(new XYChart.Data<>(mainController.getExecutionEnvironment().steps(), mainController.getExecutionEnvironment().currentState().getOccupancy(0)));
        index1.getData().add(new XYChart.Data<>(mainController.getExecutionEnvironment().steps(), mainController.getExecutionEnvironment().currentState().getOccupancy(1)));
        index2.getData().add(new XYChart.Data<>(mainController.getExecutionEnvironment().steps(), mainController.getExecutionEnvironment().currentState().getOccupancy(2)));
        index3.getData().add(new XYChart.Data<>(mainController.getExecutionEnvironment().steps(), mainController.getExecutionEnvironment().currentState().getOccupancy(3)));
        index4.getData().add(new XYChart.Data<>(mainController.getExecutionEnvironment().steps(), mainController.getExecutionEnvironment().currentState().getOccupancy(4)));

        seriesList.setAll(index0,index1,index2,index3,index4);
        this.areaChartView.setData(seriesList);
    }

     */

    private ExecutionEnvironment<PopulationState> getEe(){
        return mainController.getExecutionEnvironment();
    }



}