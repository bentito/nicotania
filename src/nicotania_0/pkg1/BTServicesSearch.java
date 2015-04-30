/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicotania_0.pkg1;

/**
 *
 * @author bentito
 */
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.bluetooth.*;
import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.pcdriver.CallbackObject;
import com.shimmerresearch.pcdriver.ShimmerPC;

/**
 *
 * Minimal Services Search example.
 */
public class BTServicesSearch {

    static final UUID OBEX_OBJECT_PUSH = new UUID(0x1105);

    static final UUID OBEX_FILE_TRANSFER = new UUID(0x1106);

    private static final java.util.UUID MY_UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static String address = "00:06:66:42:23:41"; // Shimmer2r, #2

    public static final Vector<String> serviceFound = new Vector<>();

    public void BTServicesSearch() throws IOException, InterruptedException {

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // First run RemoteDeviceDiscovery and use discoved device
        BTDeviceDiscovery.main(null);

        serviceFound.clear();

//        UUID serviceUUID = OBEX_OBJECT_PUSH;
        UUID serviceUUID = OBEX_OBJECT_PUSH;

        if ((args != null) && (args.length > 0)) {
            serviceUUID = new UUID(args[0], false);
        }

        final Object serviceSearchCompletedEvent = new Object();

        DiscoveryListener listener = new DiscoveryListener() {

            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            }

            public void inquiryCompleted(int discType) {
            }

            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                for (int i = 0; i < servRecord.length; i++) {
                    String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                    if (url == null) {
                        continue;
                    }
                    serviceFound.add(url);
                    DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
                    if (serviceName != null) {
                        System.out.println("service " + serviceName.getValue() + " found " + url);
                    } else {
                        System.out.println("service found " + url);
                    }
                }
            }

            public void serviceSearchCompleted(int transID, int respCode) {
                System.out.println("service search completed!");
                synchronized (serviceSearchCompletedEvent) {
                    serviceSearchCompletedEvent.notifyAll();
                }
            }

        };

        UUID[] searchUuidSet = new UUID[]{serviceUUID};
        int[] attrIDs = new int[]{
            0x0100 // Service name
        };

        for (Enumeration en = BTDeviceDiscovery.devicesDiscovered.elements(); en.hasMoreElements();) {
            RemoteDevice btDevice = (RemoteDevice) en.nextElement();

            synchronized (serviceSearchCompletedEvent) {
                System.out.println("search services on " + btDevice.getBluetoothAddress() + " " + btDevice.getFriendlyName(false));
                if ("00066646B6A7".equals(btDevice.getBluetoothAddress())) {
                    System.out.println("debug: found the droid I'm looking for.");
                
//                private BluetoothDevice device = null;
//                btSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                NicShimmerObject sbt = new NicShimmerObject();
                sbt.connect(btDevice.getBluetoothAddress(), "default");
//                WaitForData waitForData = new WaitForData(sbt);
                sbt.toggleLed();
                sbt.startStreaming();
                sbt.stopStreaming();
//                LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, btDevice, listener);
                serviceSearchCompletedEvent.wait();
            }
        }
    }

}

/*    private void start() throws IOException, InterruptedException {
 // First run RemoteDeviceDiscovery and use discovered device
 BTDeviceDiscovery.main(null);

 serviceFound.clear();

 //        UUID serviceUUID = OBEX_OBJECT_PUSH;
 UUID serviceUUID = OBEX_OBJECT_PUSH;

 //        if ((args != null) && (args.length > 0)) {
 //            serviceUUID = new UUID(args[0], false);
 //        }
 final Object serviceSearchCompletedEvent = new Object();

 DiscoveryListener listener = new DiscoveryListener() {

 public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
 }

 public void inquiryCompleted(int discType) {
 }

 public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
 for (int i = 0; i < servRecord.length; i++) {
 String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
 if (url == null) {
 continue;
 }
 serviceFound.add(url);
 DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
 if (serviceName != null) {
 System.out.println("service " + serviceName.getValue() + " found " + url);
 } else {
 System.out.println("service found " + url);
 }
 }
 }

 public void serviceSearchCompleted(int transID, int respCode) {
 System.out.println("service search completed!");
 synchronized (serviceSearchCompletedEvent) {
 serviceSearchCompletedEvent.notifyAll();
 }
 }

 };

 UUID[] searchUuidSet = new UUID[]{serviceUUID};
 int[] attrIDs = new int[]{
 0x0100 // Service name
 };

 for (Enumeration en = BTDeviceDiscovery.devicesDiscovered.elements(); en.hasMoreElements();) {
 RemoteDevice btDevice = (RemoteDevice) en.nextElement();

 synchronized (serviceSearchCompletedEvent) {
 System.out.println("search services on " + btDevice.getBluetoothAddress() + " " + btDevice.getFriendlyName(false));
 //                private BluetoothDevice device = null;
 //                btSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
 NicShimmerObject sbt = new NicShimmerObject();
 sbt.connect(btDevice.getBluetoothAddress(), "default");
 //                WaitForData waitForData = new WaitForData(sbt);
 sbt.toggleLed();
 sbt.startStreaming();
 sbt.stopStreaming();
 //                LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, btDevice, listener);
 serviceSearchCompletedEvent.wait();
 }
 }

 }
 }

 class WaitForData implements com.shimmerresearch.pcdriver.Callable {

 public WaitForData(ShimmerPC shimmer) {
 shimmer.passCallback(this);
 }

 public void callBackMethod(int ind, Object objectCluster) {
 if (ind == ShimmerPC.MSG_IDENTIFIER_STATE_CHANGE) {
 CallbackObject callbackObject = (CallbackObject) objectCluster;
 int state = callbackObject.mIndicator;
 if (state == ShimmerBluetooth.STATE_CONNECTING) {	//Never called
 System.out.println("Shimmer Connecting");
 } else if (state == ShimmerBluetooth.STATE_CONNECTED) {
 System.out.println("Shimmer Connected");
 //					connected();
 } else {
 System.out.println("Shimmer Disconnected");
 //					onDisconnect();
 }
 } else if (ind == ShimmerPC.MSG_IDENTIFIER_NOTIFICATION_MESSAGE) {
 CallbackObject callbackObject = (CallbackObject) objectCluster;
 int msg = callbackObject.mIndicator;
 if (msg == ShimmerPC.NOTIFICATION_STOP_STREAMING) {

 //					if (mShimmer.getShimmerVersion()==SHIMMER_3 || mShimmer.getShimmerVersion()==SHIMMER_SR30) {
 //						menuItemExgSettings.setEnabled(true);
 //					}
 } else if (msg == ShimmerPC.NOTIFICATION_START_STREAMING) {

 } else {	//Ready for Streaming

 }
 } else if (ind == ShimmerPC.MSG_IDENTIFIER_DATA_PACKET) {
 ObjectCluster objc = (ObjectCluster) objectCluster;
 String[] exgnames = {"EXG1 CH1", "EXG1 CH2", "EXG2 CH1", "EXG2 CH2", "ECG LL-RA", "ECG LA-RA", "ECG Vx-RL", "EMG CH1", "EMG CH2", "EXG1 CH1 16Bit", "EXG1 CH2 16Bit", "EXG2 CH1 16Bit", "EXG2 CH2 16Bit"};
 //Filter signals
 if (highPassFilterEnabled || bandStopFilterEnabled){
 for (int indexgnames=0;indexgnames<exgnames.length;indexgnames++){
 Collection<FormatCluster> cf = objc.mPropertyCluster.get(exgnames[indexgnames]);
 if (cf.size()!=0){
 double data =((FormatCluster)ObjectCluster.returnFormatCluster(cf,"CAL")).mData;
 if (exgnames[indexgnames].equals("EXG1 CH1")) {
 if (highPassFilterEnabled){
 data = hpfexg1ch1.filterData(data);
 }
 if (bandStopFilterEnabled){
 data = bsfexg1ch1.filterData(data);
 }
 ((FormatCluster)ObjectCluster.returnFormatCluster(cf,"CAL")).mData = data;
 exg1Ch1Data=data;
 } else if (exgnames[indexgnames].equals("EXG1 CH2")) {
 if (highPassFilterEnabled){
 data = hpfexg1ch2.filterData(data);
 }
 if (bandStopFilterEnabled){
 data = bsfexg1ch2.filterData(data);
 }
 ((FormatCluster)ObjectCluster.returnFormatCluster(cf,"CAL")).mData = data;
 exg1Ch2Data=data;
 } else if (exgnames[indexgnames].equals("EXG2 CH1")) {
 if (highPassFilterEnabled){
 data = hpfexg2ch1.filterData(data);
 }
 if (bandStopFilterEnabled){
 data = bsfexg2ch1.filterData(data);
 }
 ((FormatCluster)ObjectCluster.returnFormatCluster(cf,"CAL")).mData = data;
 exg2Ch1Data=data;
 } else if (exgnames[indexgnames].equals("EXG2 CH2")) {
 if (highPassFilterEnabled){
 data = hpfexg2ch2.filterData(data);
 }
 if (bandStopFilterEnabled){
 data = bsfexg2ch2.filterData(data);
 }
 ((FormatCluster)ObjectCluster.returnFormatCluster(cf,"CAL")).mData = data;
 exg2Ch2Data=data;
 } else if (exgnames[indexgnames].equals("ECG LL-RA")) {
 if (highPassFilterEnabled){
 data = hpfexg1ch1.filterData(data); 
 }
 if (bandStopFilterEnabled){
 data = bsfexg1ch1.filterData(data);
 }
 ((FormatCluster)ObjectCluster.returnFormatCluster(cf,"CAL")).mData = data;
 exg1Ch1Data=data;
 } else if (exgnames[indexgnames].equals("ECG LA-RA")) {
 if (highPassFilterEnabled){
 data = hpfexg1ch2.filterData(data);
 }
 if (bandStopFilterEnabled){
 data = bsfexg1ch2.filterData(data);
 }
 ((FormatCluster)ObjectCluster.returnFormatCluster(cf,"CAL")).mData = data;
 exg1Ch2Data=data;
 } else if (exgnames[indexgnames].equals("ECG Vx-RL")) {
 if (highPassFilterEnabled){
 data = hpfexg2ch2.filterData(data);
 }
 if (bandStopFilterEnabled){
 data = bsfexg2ch2.filterData(data);
 }
 ((FormatCluster)ObjectCluster.returnFormatCluster(cf,"CAL")).mData = data;
 exg2Ch2Data=data;
 } else if (exgnames[indexgnames].equals("EMG CH1")) {
 if (highPassFilterEnabled){
 data = hpfexg1ch1.filterData(data); 
 }
 if (bandStopFilterEnabled){
 data = bsfexg1ch1.filterData(data);
 }
 ((FormatCluster)ObjectCluster.returnFormatCluster(cf,"CAL")).mData = data;
 exg1Ch1Data=data;
 } else if (exgnames[indexgnames].equals("EMG CH2")) {
 if (highPassFilterEnabled){
 data = hpfexg1ch2.filterData(data);
 }
 if (bandStopFilterEnabled){
 data = bsfexg1ch2.filterData(data);
 }
 ((FormatCluster)ObjectCluster.returnFormatCluster(cf,"CAL")).mData = data;
 exg1Ch2Data=data;
 } else if (exgnames[indexgnames].equals("EXG1 CH1 16Bit")) {
 if (highPassFilterEnabled){
 data = hpfexg1ch1.filterData(data);
 }
 if (bandStopFilterEnabled){
 data = bsfexg1ch1.filterData(data);
 }
 ((FormatCluster)ObjectCluster.returnFormatCluster(cf,"CAL")).mData = data;
 exg1Ch1Data=data;
 } else if (exgnames[indexgnames].equals("EXG1 CH2 16Bit")) {
 if (highPassFilterEnabled){
 data = hpfexg1ch2.filterData(data);
 }
 if (bandStopFilterEnabled){
 data = bsfexg1ch2.filterData(data);
 }
 ((FormatCluster)ObjectCluster.returnFormatCluster(cf,"CAL")).mData = data;
 exg1Ch2Data=data;
 } else if (exgnames[indexgnames].equals("EXG2 CH2 16Bit")) {
 if (highPassFilterEnabled){
 data = hpfexg2ch2.filterData(data);
 }
 if (bandStopFilterEnabled){
 data = bsfexg2ch1.filterData(data);
 }
 ((FormatCluster)ObjectCluster.returnFormatCluster(cf,"CAL")).mData = data;
 exg2Ch1Data=data;
 } else if (exgnames[indexgnames].equals("EXG2 CH2 16Bit")) {
 if (highPassFilterEnabled){
 data = hpfexg2ch2.filterData(data);
 }
 if (bandStopFilterEnabled){
 data = bsfexg2ch2.filterData(data);
 }
 ((FormatCluster)ObjectCluster.returnFormatCluster(cf,"CAL")).mData = data;
 exg2Ch2Data=data;
 }
 }
 }
 } */

/*				String[] selectedSensorSignals = selectSignalsToView();
 int numberOfSelectedSignals = selectedSensorSignals.length;
 if (numberOfSelectedSignals>maxTraces) {
 numberOfSelectedSignals=maxTraces;
 }
 double dataArrayPPG = 0;
 double heartRate = Double.NaN;
 */
/*				if (numberOfSelectedSignals > 0 || calculateHeartRate) {
 chart.removeAllTraces();
 Collection<FormatCluster> formats[] = new Collection[numberOfSelectedSignals];
 FormatCluster cal[] = new FormatCluster[numberOfSelectedSignals];
 double[] dataArray = new double[numberOfSelectedSignals];
 for (int count=0; count<numberOfSelectedSignals; count++) {
 chart.addTrace(traces[count]);
 traces[count].setVisible(true);
 traces[count].setName(selectedSensorSignals[count]);
 formats[count] = objc.mPropertyCluster.get(selectedSensorSignals[count]);
 cal[count] = ((FormatCluster)ObjectCluster.returnFormatCluster(formats[count],"CAL"));
 if (cal[count]!=null) {
 if (calibrated[count]) {
 dataArray[count] = ((FormatCluster)ObjectCluster.returnFormatCluster(formats[count],"CAL")).mData;
 } else {
 dataArray[count] = ((FormatCluster)ObjectCluster.returnFormatCluster(formats[count],"RAW")).mData;
 }
 }
 }
					
 Multimap<String, FormatCluster> m = objc.mPropertyCluster;
 for(String key : m.keys()) {
 if (key.equals("Internal ADC A13")){
							
 Collection<FormatCluster> format;
 FormatCluster calPPG;
 format = objc.mPropertyCluster.get("Internal ADC A13");
 calPPG = ((FormatCluster)ObjectCluster.returnFormatCluster(format,"CAL"));
 if (calPPG!=null) {
 dataArrayPPG = (int) ((FormatCluster)ObjectCluster.returnFormatCluster(format,"CAL")).mData;
 dataArrayPPG = lpf.filterData(dataArrayPPG);
 dataArrayPPG = hpf.filterData(dataArrayPPG);
 }
 }
 }
					
 if (calculateHeartRate){
 heartRate = heartRateCalculation.ppgToHrConversion(dataArrayPPG);
 if (heartRate == INVALID_RESULT){
 heartRate = Double.NaN;
 }
 objc.mPropertyCluster.put("Heart Rate",new FormatCluster("CAL","beats per minute",heartRate));
 if (chckbxHeartRate.isSelected()) {
 chart.addTrace(traceHR);
 }
 }
					
 //Plotting data
 int numberOfTraces = dataArray.length;
 for (int i=0; i<numberOfTraces; i++){
 float newX = mLastX + mSpeed;
 if (chckbxHeartRate.isSelected()){
 traceHR.addPoint(newX, heartRate);
 }
 for (int count=0; count<numberOfTraces; count++) {
 traces[count].addPoint(newX, dataArray[count]);
 if (count==0) {
 mLastX += mSpeed;
 }
 }
 }
 if (numberOfTraces == 0 && chckbxHeartRate.isSelected()){
 float newX = mLastX + mSpeed;
 traceHR.addPoint(newX, heartRate);
 mLastX += mSpeed;
 minDataPoint=-5;
 maxDataPoint=215;
 Range range = new Range(minDataPoint, maxDataPoint);
 IRangePolicy rangePolicy = new RangePolicyFixedViewport(range);
 yAxis.setRangePolicy(rangePolicy);
 } else {
 //Scaling Y Axis
 for (int count=0; count<numberOfTraces; count++){
 if (dataArray[count] > maxDataPoint) {
 maxDataPoint = (int) Math.ceil(dataArray[count]);
 }
 if (heartRate > maxDataPoint){
 maxDataPoint = (int) Math.ceil(heartRate);
 }
 if (dataArray[count] < minDataPoint) {
 minDataPoint = (int) Math.floor(dataArray[count]);
 }
 if (heartRate < minDataPoint) {
 minDataPoint = (int) Math.floor(heartRate);
 }
 }
 Range range = new Range(minDataPoint, maxDataPoint);
 IRangePolicy rangePolicy = new RangePolicyFixedViewport(range);
 yAxis.setRangePolicy(rangePolicy);
 }
 }
				
 if (returnVal == JFileChooser.APPROVE_OPTION && loggingData) {
 log.logData(objc);
 }
				
				
 } else if (ind == ShimmerPC.MSG_IDENTIFIER_PACKET_RECEPTION_RATE) {
 double packetReceptionRate = (Double) objectCluster;
 textFieldMessage.setText("Packet Reception Rate: " + Double.toString(packetReceptionRate));
 }
             
 }
 }

 public void directMethod() {

 }*/
}
