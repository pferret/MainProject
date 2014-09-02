/**
 * 
 */
package guiTabs;

/**
 * @author roland
 *
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import order.Order;
import order.OrderDB;
import order.StockDBControl;
import order.StockItem;
import person.Person;
import person.Staff;
import person.Supplier;
import retailSystem.PersonDB;
import retailSystem.Product;

public class SupplyOrderTab extends JPanel implements ActionListener, ItemListener,
		ListSelectionListener, ComponentListener {

	ArrayList<StockItem> orderStockItemList;
	ArrayList<ArrayList<Product>> productList;
	private Supplier supplier;
	private Supplier customer;

	private MainGUI gui;
	private StockDBControl stockDBControl;
	private PersonDB personDB;
	private OrderDB orderDB;
	private Staff currentlyLoggedInStaff;
	private StockControlTab stockControlTab;
	private NumberFormat formatter = new DecimalFormat("#0.00");

	protected Vector<String> comboBoxItems = new Vector<String>();
	protected ArrayList<Integer> itemsQuantity = new ArrayList<Integer>();
	protected ArrayList<Double> itemsPrice = new ArrayList<Double>();
	protected DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<String>(
			comboBoxItems);
	protected JComboBox<String> productComboBox = new JComboBox<String>(comboBoxModel);

	protected Vector<String> supplierComboBoxItems = new Vector<String>();
	protected DefaultComboBoxModel<String> supplierComboBoxModel = new DefaultComboBoxModel<String>(
			supplierComboBoxItems);
	protected JComboBox<String> supplierComboBox = new JComboBox<String>(supplierComboBoxModel);

	protected DefaultListModel<String> listModel = new DefaultListModel<String>();
	protected JList<String> orderingList = new JList<String>(listModel);
	protected JScrollPane orderingListSroll = new JScrollPane(orderingList);
	private JScrollBar scrollBar = orderingListSroll.getVerticalScrollBar();

	protected ArrayList<Product> currentOrderList = new ArrayList<Product>();
	protected ArrayList<Integer> currentOrderListQuantity = new ArrayList<Integer>();
	protected ArrayList<Integer> currentOrderComboIndex = new ArrayList<Integer>();

	protected JLabel productComboBoxLabel, quantityLabel, availableQuantityLabel,
			availableQuantityField, priceLabel, priceField, grandTotalLabel, grandTotalField,
			orderListProductLabel, orderListQtyLabel, orderListPriceLabel,
			orderListTotalPriceLabel, supplierComboBoxLabel, supplierIDLabel, supplierIDField;
	protected JTextField quantityTextField;
	protected JButton addButton, removeButton, processButton, cancelButton;
	protected double grandTotal = 0;

	private TabListCellRenderer renderer = new TabListCellRenderer(false);
	// source for this class:
	// http://www.grandt.com/sbe/files/uts2/Chapter10html/Chapter10.htm

	private JPanel mainPanel, outerPanel;
	private int xSize;
	private int ySize;
	private int xPosition;
	private int yPosition;
	private boolean selected;

	public SupplyOrderTab(Staff currentlyLoggedInStaff, PersonDB personDB,
			StockDBControl stockDBControl, OrderDB orderDB, MainGUI gui) {

		this.gui = gui;
		this.stockControlTab = gui.getStockControlTab();
		this.currentlyLoggedInStaff = currentlyLoggedInStaff;
		this.orderDB = orderDB;
		this.personDB = personDB;
		this.stockDBControl = stockDBControl;

		xSize = 530;
		ySize = 350;
		mainPanel = new JPanel();
		mainPanel.setLayout(null);
		outerPanel = new JPanel();
		outerPanel.setLayout(null);
		outerPanel.add(mainPanel);
		selected = false;
		outerPanel.setBorder(BorderFactory.createLineBorder(new Color(176, 168, 168), 8, true));
		this.setBorder(BorderFactory.createLineBorder(new Color(176, 168, 168), 4));
		add(outerPanel, new GridBagConstraints());
		addComponentListener(this);

		setUpSupplierComboBox();
		setUpProductComboBox();

		setUpSupplierNameField();
		setUpQuantityTextField();
		setUpAvailableQuantity();
		setUpPriceFields();
		setAddButton();
		setRemoveButton();
		setProcessButton();
		setCancelButton();
		setUpSubtotal();
		orderingList();
		refreshTab(0);

		setLayout(null);
		setVisible(true);
	}

	public OrderDB getOrderDB() {
		return orderDB;
	}

	// add customer combo box
	private void setUpSupplierComboBox() {
		supplierComboBoxLabel = new JLabel("Suppliers");
		supplierComboBoxLabel.setBounds(59, 33, 93, 20);
		mainPanel.add(supplierComboBoxLabel);
		supplierComboBox.setBounds(200, 30, 265, 23);
		mainPanel.add(supplierComboBox);
		fillUpSupplierComboBox();
		supplierComboBox.addItemListener(this);
	}

	// add product combo box
	private void setUpProductComboBox() {
		productComboBoxLabel = new JLabel("Products");
		productComboBoxLabel.setBounds(59, 55, 265, 20);
		productComboBox.setBounds(200, 55, 265, 23);
		mainPanel.add(productComboBoxLabel);
		mainPanel.add(productComboBox);
		fillUpProductComboBox();
		productComboBox.addItemListener(this);
	}

	// add customer name field
	private void setUpSupplierNameField() {
		supplierIDLabel = new JLabel("Supplier ID");
		supplierIDLabel.setBounds(59, 10, 93, 20);
		supplierIDField = new JLabel();
		supplierIDField.setBounds(203, 7, 265, 23);
		mainPanel.add(supplierIDLabel);
		mainPanel.add(supplierIDField);
	}

	// add product quantity field
	private void setUpQuantityTextField() {
		quantityLabel = new JLabel("Order Quantity");
		quantityLabel.setBounds(59, 130, 110, 20);
		quantityTextField = new JTextField("1");
		quantityTextField.setBounds(200, 133, 50, 23);
		mainPanel.add(quantityLabel);
		mainPanel.add(quantityTextField);
	}

	// add product available quantity
	private void setUpAvailableQuantity() {
		availableQuantityLabel = new JLabel("Quantity in Stock");
		availableQuantityLabel.setBounds(59, 83, 140, 20);
		int quantity = 0;
		if (itemsQuantity.size() > 0) {
			quantity = itemsQuantity.get(0);
		}
		availableQuantityField = new JLabel(Integer.toString(quantity));
		availableQuantityField.setBounds(200, 83, 93, 20);
		mainPanel.add(availableQuantityLabel);
		mainPanel.add(availableQuantityField);
	}

	// add product price fields
	private void setUpPriceFields() {
		priceLabel = new JLabel("Unit Price");
		priceLabel.setBounds(59, 108, 93, 14);
		double price = 0;
		if (itemsPrice.size() > 0) {
			price = itemsPrice.get(0);
		}
		priceField = new JLabel(formatter.format(price));
		priceField.setBounds(200, 108, 93, 14);
		mainPanel.add(priceLabel);
		mainPanel.add(priceField);
	}

	// add Grand Total price fields
	private void setUpSubtotal() {
		grandTotalLabel = new JLabel("Grand Total:");
		grandTotalLabel.setBounds(335, 320, 130, 23);
		grandTotalField = new JLabel("0.00");
		grandTotalField.setBounds(350, 320, 130, 23);

		grandTotalField.setHorizontalAlignment(JLabel.RIGHT);
		mainPanel.add(grandTotalLabel);
		mainPanel.add(grandTotalField);
	}

	// add add button
	private void setAddButton() {
		addButton = new JButton("+");
		addButton.setFont(new Font("Arial", Font.BOLD, 22));
		addButton.setBounds(270, 133, 93, 20);
		addButton.addActionListener(this);
		mainPanel.add(addButton);
	}

	// add remove button
	private void setRemoveButton() {
		removeButton = new JButton("-");
		removeButton.setFont(new Font("Arial", Font.BOLD, 35));

		removeButton.setBounds(380, 133, 93, 20);
		removeButton.addActionListener(this);
		mainPanel.add(removeButton);
		removeButton.setEnabled(false);
	}

	// add process button
	private void setProcessButton() {
		processButton = new JButton("Create Order");
		processButton.setBounds(199, 320, 130, 23);
		processButton.addActionListener(this);
		mainPanel.add(processButton);
		processButton.setEnabled(false);
	}

	private void setCancelButton() {
		cancelButton = new JButton("Cancel");
		cancelButton.setBounds(64, 320, 130, 23);
		cancelButton.addActionListener(this);
		mainPanel.add(cancelButton);
		cancelButton.setEnabled(false);
	}

	// add buying list
	private void orderingList() {
		orderListProductLabel = new JLabel("Product name");
		orderListProductLabel.setBounds(59, 155, 110, 20);
		orderListQtyLabel = new JLabel("Qty");
		orderListQtyLabel.setBounds(275, 155, 30, 20);
		orderListPriceLabel = new JLabel("Price");
		orderListPriceLabel.setBounds(340, 155, 50, 20);
		orderListTotalPriceLabel = new JLabel("Total");
		orderListTotalPriceLabel.setBounds(420, 155, 50, 20);
		mainPanel.add(orderListProductLabel);
		mainPanel.add(orderListQtyLabel);
		mainPanel.add(orderListPriceLabel);
		mainPanel.add(orderListTotalPriceLabel);

		orderingList.addListSelectionListener(this);
		orderingListSroll.setBounds(59, 180, 420, 135);

		mainPanel.add(orderingListSroll);

		renderer.setTabs(new int[] { 230, 315, 395 });
		orderingList.setCellRenderer(renderer);
	}

	public JComboBox<String> getSupplierComboBox() {
		return supplierComboBox;
	}

	// fill up customer combo Box
	public void fillUpSupplierComboBox() {

		supplierComboBoxItems.clear();
		supplierComboBoxItems.add("<html><font color='red'>Select Supplier</font></html>");

		ArrayList<Person> customers = personDB.getSupplierList();
		for (Person customer : customers) {
			supplierComboBoxItems.add(customer.getName());
		}
		supplierComboBox.setSelectedIndex(customers.size() - 1);

	}

	// fill up product combo Box with price and quantity
	private void fillUpProductComboBox() {
		ArrayList<StockItem> list = stockDBControl.getStockList();
		comboBoxItems.clear();
		itemsQuantity.clear();
		itemsPrice.clear();
		for (StockItem stockItem : list) {
			System.out.println(supplierComboBox.getSelectedItem());
			if (stockItem.getProduct().getSupplier().getName().equals(
					supplierComboBox.getSelectedItem())) {
				comboBoxItems.add(stockItem.getProduct().getProductName());
				itemsPrice.add(stockItem.getProduct().getSupplierPrice());
				itemsQuantity.add(stockItem.getQuantity());
			}
		}
		productComboBox.setSelectedIndex(comboBoxItems.size() - 1);
	}

	// refresh Tab
	private void refreshTab(int index) {
		if (index > -1) {
			if (itemsPrice.size() > 0)
				priceField.setText(formatter.format(itemsPrice.get(index)));
			quantityTextField.setText("1");
			if (itemsQuantity.size() > 0) {
				availableQuantityField.setText(Integer.toString(itemsQuantity.get(index)));
				grandTotalField.setText(formatter.format(grandTotal));
				productComboBox.setSelectedIndex(index);

				if (supplierComboBox.getSelectedIndex() != 0) {
					supplierIDField.setText(Integer.toString(personDB.getSupplierList().get(
							(supplierComboBox.getSelectedIndex() - 1)).getId()));
				}
			}
		}
	}

	// combo box selection
	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getStateChange() == ItemEvent.SELECTED) {
			if (event.getItemSelectable().equals(supplierComboBox)) {
				fillUpProductComboBox();
				refreshTab(productComboBox.getSelectedIndex());
			}
		}
	}

	// button actions
	@Override
	public void actionPerformed(ActionEvent e) {

		// ADD BUTTON
		if (e.getSource() == addButton) {

			// add product and quantity to the current order list
			int index = productComboBox.getSelectedIndex();

			Product selectedProduct = stockDBControl.getStockList().get(index).getProduct();

			int availableQuantity = itemsQuantity.get(index);

			// accepting only integer into quantityTextField
			try {
				int orderingQuantity = (Integer.parseInt(quantityTextField.getText()));

				if (orderingQuantity > 0) {

					// check if there is enough product available to buy
					if (orderingQuantity <= availableQuantity) {
						processButton.setEnabled(true);
						// set available quantity
						itemsQuantity.set(index, availableQuantity - orderingQuantity);

						// refresh subtotal price
						grandTotal += (selectedProduct.getRetailPrice() * orderingQuantity);

						// check if the selected product is already in the currentOrderList
						int orderListIndex = currentOrderList.indexOf(selectedProduct);
						if (orderListIndex >= 0) {
							orderingQuantity += currentOrderListQuantity.get(orderListIndex);
							currentOrderListQuantity.set(orderListIndex, orderingQuantity);

							listModel.setElementAt(textAlignment(comboBoxItems.get(index), Integer
									.toString(orderingQuantity), priceField.getText(), formatter
									.format(orderingQuantity * selectedProduct.getRetailPrice())),
									orderListIndex);
							orderingList.setSelectedIndex(orderListIndex);

						}
						// selected product is NOT on the currntOrderList
						else {
							currentOrderList.add(selectedProduct);
							currentOrderListQuantity.add(orderingQuantity);
							listModel.addElement(textAlignment(comboBoxItems.get(index),
									quantityTextField.getText(), priceField.getText(), formatter
											.format(orderingQuantity
													* selectedProduct.getRetailPrice())));
							validate();
							scrollBar.setValue(scrollBar.getMaximum());
							orderingList.setSelectedIndex(listModel.getSize() - 1);
						}
						supplierComboBox.setEnabled(false);
						removeButton.setEnabled(true);
						cancelButton.setEnabled(true);
						refreshTab(index);
					}
					else
						JOptionPane.showMessageDialog(null, "Not enough product in stock!");
				}
				else
					JOptionPane.showMessageDialog(null,
							"Product quantity must by positive whole number");

			}
			catch (NumberFormatException error) {
				JOptionPane.showMessageDialog(null,
						"Invalid entry, only positive whole numbers please!");
				// error.printStackTrace();
			}
		}

		// REMOVE BUTTON
		if (e.getSource() == removeButton) {
			int index = orderingList.getSelectedIndex();
			int i = 0; // index in combo list or stock list

			for (StockItem stockItem : stockDBControl.getStockList()) {
				if (stockItem.getProduct().getProductID() == currentOrderList.get(index)
						.getProductID()) {
					break;
				}
				i++;
			}

			Product selectedProduct = stockDBControl.getStockList().get(i).getProduct();

			int availableQuantity = itemsQuantity.get(i);

			// accepting only integer into quantityTextField
			try {
				int orderingQuantity = (Integer.parseInt(quantityTextField.getText()));

				if (orderingQuantity > 0) {
					processButton.setEnabled(true);
					// set available quantity
					itemsQuantity.set(i, availableQuantity + orderingQuantity);
					grandTotal -= (currentOrderList.get(index).getRetailPrice() * Integer
							.parseInt(quantityTextField.getText()));

					// check if the selected product is already in the currentOrderList
					int orderListIndex = currentOrderList.indexOf(selectedProduct);
					if (orderListIndex >= 0 && (currentOrderListQuantity.get(orderListIndex) > 1)) {
						orderingQuantity = currentOrderListQuantity.get(orderListIndex)
								- (Integer.parseInt(quantityTextField.getText()));
						currentOrderListQuantity.set(orderListIndex, orderingQuantity);

						listModel.setElementAt(textAlignment(comboBoxItems.get(index), Integer
								.toString(orderingQuantity), priceField.getText(), formatter
								.format(orderingQuantity * selectedProduct.getRetailPrice())),
								orderListIndex);
						orderingList.setSelectedIndex(orderListIndex);

					}
					else {
						currentOrderList.remove(index);
						currentOrderListQuantity.remove(index);
						listModel.remove(index);

					}
					removeButton.setEnabled(true);
					cancelButton.setEnabled(true);
				}
				else
					JOptionPane.showMessageDialog(null,
							"Product quantity must by positive whole number");

			}
			catch (NumberFormatException error) {
				JOptionPane.showMessageDialog(null,
						"Invalid entry, only positive whole numbers please!");
				// error.printStackTrace();
			}
			if (listModel.isEmpty()) {
				supplierComboBox.setEnabled(true);
				removeButton.setEnabled(false);
				processButton.setEnabled(false);
				cancelButton.setEnabled(false);
			}
			refreshTab(i);
		}

		// PROCESS BUTTON
		if (e.getSource() == processButton) {

			// make new stock list, fill the elements from the chosen product and quantities
			StockItem orderStockItem = new StockItem();
			orderStockItemList = new ArrayList<StockItem>();

			for (int i = 0; i < currentOrderList.size(); i++) {
				orderStockItem = new StockItem();
				orderStockItem.setProduct(currentOrderList.get(i));
				orderStockItem.setQuantity(currentOrderListQuantity.get(i));
				orderStockItemList.add(orderStockItem);
			}
			Supplier customer = (Supplier) personDB.getSupplierList().get(
					supplierComboBox.getSelectedIndex() - 1);

			// Staff responsibleStaffForOrder = (Staff)personDB.getStaffList().get(0);

			// make a new order and put it to the orderDB
			Order order = new Order(currentlyLoggedInStaff, customer, orderStockItemList,
					grandTotal, new java.util.Date());
			orderDB.getSupplyOrderList().add(order);
			showOrders();
			// customer.getOrders().add(order);
			this.customer = customer;
			for (StockItem stockItem : orderStockItemList) {
				System.out.println("Q=" + stockItem.getQuantity() + " P="
						+ stockItem.getProduct().getProductName());

			}

			// reduce the products' quantity in stockDBControl
			int quantity;
			for (int index = 0; index < currentOrderList.size(); index++) {
				quantity = stockDBControl.getStockItem(currentOrderList.get(index).getProductID())
						.getQuantity();
				quantity -= currentOrderListQuantity.get(index);
				stockDBControl.getStockItem(currentOrderList.get(index).getProductID())
						.setQuantity(quantity);
			}

			// gui.getSupplierTab().addItemsToOrderCombobox(true);
			gui.getSupplyOrderHistorytab().setOrderList();
			showOrderDetails(order);

			// clear the tab and refresh
			listModel.clear();
			currentOrderList.clear();
			currentOrderListQuantity.clear();
			grandTotal = 0;
			supplierComboBox.setEnabled(true);
			removeButton.setEnabled(false);
			cancelButton.setEnabled(false);
			processButton.setEnabled(false);
			fillUpSupplierComboBox();
			fillUpProductComboBox();
			refreshTab(0);
			stockControlTab.refreshStockControlTab();
		}
		// CANCEL BUTTON
		if (e.getSource() == cancelButton) {
			do {

				int index = orderingList.getSelectedIndex();
				int i = 0; // index in combo list or stock list

				for (StockItem stockItem : stockDBControl.getStockList()) {
					if (stockItem.getProduct().getProductID() == currentOrderList.get(index)
							.getProductID()) {
						break;
					}
					i++;
				}

				grandTotal -= (currentOrderList.get(index).getRetailPrice() * currentOrderListQuantity
						.get(index));
				itemsQuantity.set(i, itemsQuantity.get(i) + currentOrderListQuantity.get(index));
				currentOrderList.remove(index);
				currentOrderListQuantity.remove(index);
				listModel.remove(index);

				if (listModel.isEmpty()) {
					supplierComboBox.setEnabled(true);
					removeButton.setEnabled(false);
					processButton.setEnabled(false);
				}
				if (index != 0)
					orderingList.setSelectedIndex(index - 1);
				else
					orderingList.setSelectedIndex(0);
				refreshTab(i);
			}
			while (listModel.getSize() != 0);
			cancelButton.setEnabled(false);
		}
	}

	private void showOrders() {
		System.out.println("**********************Supplier Orders*************************");

		for (int i = 0; i < orderDB.getSupplyOrderList().size(); i++) {
			Order order = (orderDB.getSupplyOrderList().get(i));
			System.out.println("Order " + order.getId() + " was created at " + order.getDate()
					+ " by " + order.getCurrentlyLoggedInStaff().getName());
			System.out.println("Supplier : " + ((Supplier) order.getPerson()).getName());
			System.out.println("Items in this order :");
			for (int j = 0; j < order.getOrderEntryList().size(); j++) {
				StockItem stockItem = order.getOrderEntryList().get(j);
				System.out.println("  " + stockItem.getQuantity() + "  "
						+ stockItem.getProduct().getProductName() + "  "
						+ (stockItem.getProduct().getRetailPrice() * stockItem.getQuantity()));
			}
			System.out.println("The total order value is " + order.getGrandTotalOfOrder() + "\n");
		}

	}

	private void showOrderDetails(Order order) {
		String orderDetailsMessage = "New order was created at " + order.getDate();
		orderDetailsMessage += "\nStaff ID : " + order.getCurrentlyLoggedInStaff().getId();
		orderDetailsMessage += "\nSupplier ID : " + order.getPerson().getId() + " Supplier name : "
				+ order.getPerson().getName();
		orderDetailsMessage += "\nItems in this order : ";
		for (StockItem stockItem : order.getOrderEntryList()) {
			orderDetailsMessage += "\n   " + stockItem.getQuantity() + " x "
					+ stockItem.getProduct().getProductName() + " \t Subtotal \t\u20ac"
					+ (stockItem.getProduct().getRetailPrice() * stockItem.getQuantity());
		}

		orderDetailsMessage += "\nThe total order value is \t\u20ac" + order.getGrandTotalOfOrder()
				+ "\n";

		JOptionPane.showMessageDialog(null, orderDetailsMessage, "Order ID : "
				+ (Order.getUniqueId() - 1), JOptionPane.PLAIN_MESSAGE);
	}

	private void setSelectedProductComboboxItem(String productName) {
		for (String str : comboBoxItems) {
			if (str.equals(productName))
				productComboBox.setSelectedItem(str);
		}
	}

	// list selection event
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == orderingList) {
			System.out.println(orderingList.getSelectedIndex());
			// check if order list is empty
			if (orderingList.getSelectedIndex() > -1) {
				// separate the text in the selected list item
				String[] values = orderingList.getSelectedValue().split("\\t");
				// call method to set the current combobox item according to the product name of the
				// selected list item
				setSelectedProductComboboxItem(values[0]);
			}
			removeButton.setEnabled(true);
			cancelButton.setEnabled(true);
		}
	}

	private String textAlignment(String text1, String text2, String text3, String text4) {
		String s = text1;
		s += "\t" + text2 + "\t" + text3 + "\t" + text4;
		return s;
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentResized(ComponentEvent e) {

		xPosition = ((((int) mainPanel.getParent().getSize().getWidth()) - xSize) / 2);
		yPosition = ((((int) mainPanel.getParent().getSize().getHeight()) - ySize) / 2);
		mainPanel.setBounds(xPosition, yPosition, xSize, ySize);

		xPosition = ((((int) outerPanel.getParent().getSize().getWidth()) - xSize) / 2);
		yPosition = ((((int) outerPanel.getParent().getSize().getHeight()) - ySize) / 2);
		outerPanel.setBounds(xPosition - 50, yPosition - 40, xSize + 100, ySize + 80);

	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub

	}
}
