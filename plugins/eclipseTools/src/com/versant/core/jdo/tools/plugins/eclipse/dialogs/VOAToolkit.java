
/*
 * Copyright (c) 1998 - 2005 Versant Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Versant Corporation - initial API and implementation
 */
package com.versant.core.jdo.tools.plugins.eclipse.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.event.PaintEvent;
import java.awt.im.InputMethodHighlight;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.*;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

public class VOAToolkit {

	private Dialog blocker;
	
	public VOAToolkit(){
		blocker = new Dialog(new Frame(), true){
		    public Toolkit getToolkit() {
				return new Toolkit(){
				public int getScreenResolution() throws HeadlessException {
					return Toolkit.getDefaultToolkit().getScreenResolution();
				}
			
				public void beep() {
					Toolkit.getDefaultToolkit().beep();
				}
			
				public void sync() {
					Toolkit.getDefaultToolkit().sync();
				}
			
				public Dimension getScreenSize() throws HeadlessException {
					return Toolkit.getDefaultToolkit().getScreenSize();
				}
			
				protected EventQueue getSystemEventQueueImpl() {
					return Toolkit.getDefaultToolkit().getSystemEventQueue();
				}
			
				public Image createImage(byte[] imagedata, int imageoffset, int imagelength) {
					return Toolkit.getDefaultToolkit().createImage(imagedata, imageoffset, imagelength);
				}
			
				public Clipboard getSystemClipboard() throws HeadlessException {
					return Toolkit.getDefaultToolkit().getSystemClipboard();
				}
			
				public ColorModel getColorModel() throws HeadlessException {
					return Toolkit.getDefaultToolkit().getColorModel();
				}
			
				public String[] getFontList() {
					return Toolkit.getDefaultToolkit().getFontList();
				}
			
				public FontMetrics getFontMetrics(Font font) {
					return Toolkit.getDefaultToolkit().getFontMetrics(font);
				}
			
				public Image createImage(ImageProducer producer) {
					return Toolkit.getDefaultToolkit().createImage(producer);
				}
			
				public Image createImage(String filename) {
					return Toolkit.getDefaultToolkit().createImage(filename);
				}
			
				public Image getImage(String filename) {
					return Toolkit.getDefaultToolkit().getImage(filename);
				}
			
				public Image createImage(URL url) {
					return Toolkit.getDefaultToolkit().createImage(url);
				}
			
				public Image getImage(URL url) {
					return Toolkit.getDefaultToolkit().getImage(url);
				}
			
				public DragSourceContextPeer createDragSourceContextPeer(
						DragGestureEvent dge) throws InvalidDnDOperationException {
					return Toolkit.getDefaultToolkit().createDragSourceContextPeer(dge);
				}
			
				public int checkImage(Image image, int width, int height,
						ImageObserver observer) {
					return Toolkit.getDefaultToolkit().checkImage(image, width, height, observer);
				}
			
				public boolean prepareImage(Image image, int width, int height,
						ImageObserver observer) {
					return Toolkit.getDefaultToolkit().prepareImage(image, width, height, observer);
				}
			
				protected ButtonPeer createButton(Button target) throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected CanvasPeer createCanvas(Canvas target) {
					throw new HeadlessException();
				}
			
				protected CheckboxMenuItemPeer createCheckboxMenuItem(
						CheckboxMenuItem target) throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected CheckboxPeer createCheckbox(Checkbox target)
						throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected ChoicePeer createChoice(Choice target) throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected DialogPeer createDialog(Dialog target) throws HeadlessException {
					return new DialogPeer() {
						public void setResizable(boolean resizeable) {
						}
			
						public void setTitle(String title) {
						}
			
						public void toBack() {
						}
			
						public void toFront() {
						}
			
						public void beginLayout() {
						}
			
						public void beginValidate() {
						}
			
						public void endLayout() {
						}
			
						public void endValidate() {
						}
			
						public boolean isPaintPending() {
							return false;
						}
			
						public Insets getInsets() {
							return new Insets(0,0,0,0);
						}
			
						public Insets insets() {
							return getInsets();
						}
			
						public void destroyBuffers() {
			
						}
			
						public void disable() {
						}
			
						public void dispose() {
						}
			
						public void enable() {
						}
			
						public void hide() {
						}
			
						public void show() {
						}
			
						public void updateCursorImmediately() {
						}
			
						public boolean canDetermineObscurity() {
							return false;
						}
			
						public boolean handlesWheelScrolling() {
							return true;
						}
			
						public boolean isFocusable() {
							return false;
						}
			
						public boolean isObscured() {
							return true;
						}
			
						public void reshape(int x, int y, int width, int height) {
						}
			
						public void setBounds(int x, int y, int width, int height) {
						}
			
						public void repaint(long tm, int x, int y, int width, int height) {
						}
			
						public void setEnabled(boolean b) {
						}
			
						public void setVisible(boolean b) {
						}
			
						public void handleEvent(AWTEvent e) {
						}
			
						public void createBuffers(int numBuffers, BufferCapabilities caps)
								throws AWTException {
						}
			
						public void flip(FlipContents flipAction) {
						}
			
						public void setBackground(Color c) {
						}
			
						public void setForeground(Color c) {
						}
			
						public boolean requestFocus(Component lightweightChild,
								boolean temporary, boolean focusedWindowChangeAllowed,
								long time) {
							return false;
						}
			
						public Dimension getMinimumSize() {
							return new Dimension(0,0);
						}
			
						public Dimension getPreferredSize() {
							return getMinimumSize();
						}
			
						public Dimension minimumSize() {
							return getMinimumSize();
						}
			
						public Dimension preferredSize() {
							return getMinimumSize();
						}
			
						public void setFont(Font f) {
						}
			
						public Graphics getGraphics() {
							return null;
						}
			
						public void paint(Graphics g) {
						}
			
						public void print(Graphics g) {
						}
			
						public GraphicsConfiguration getGraphicsConfiguration() {
							return null;
						}
			
						public Image getBackBuffer() {
							return null;
						}
			
						public Image createImage(int width, int height) {
							return null;
						}
			
						public Point getLocationOnScreen() {
							return new Point(0,0);
						}
			
						public Toolkit getToolkit() {
							return null;
						}
			
						public void coalescePaintEvent(PaintEvent e) {
			
						}
			
						public ColorModel getColorModel() {
							return null;
						}
			
						public VolatileImage createVolatileImage(int width, int height) {
							return null;
						}
			
						public FontMetrics getFontMetrics(Font font) {
							return null;
						}
			
						public Image createImage(ImageProducer producer) {
							return null;
						}
			
						public int checkImage(Image img, int w, int h, ImageObserver o) {
							return 0;
						}
			
						public boolean prepareImage(Image img, int w, int h, ImageObserver o) {
							return false;
						}

                        // new JDK 1.5 methods

                        public void updateAlwaysOnTop() {
                        }

                        public boolean requestWindowFocus() {
                            return false;
                        }

                        public void cancelPendingPaint(int x, int y, int w, int h) {
                        }

                        public void restack() {
                        }

                        public boolean isRestackSupported() {
                            return false;
                        }

                        public void setBounds(int x, int y, int width, int height, int op) {
                        }

                        public void reparent(ContainerPeer newContainer) {
                        }

                        public boolean isReparentSupported() {
                            return false;
                        }

                        public void layout() {
                        }

                        public Rectangle getBounds() {
                            return null;
                        }
					};
				}
			
				protected FileDialogPeer createFileDialog(FileDialog target)
						throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected FontPeer getFontPeer(String name, int style) {
					throw new HeadlessException();
				}
			
				protected FramePeer createFrame(Frame target) throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected LabelPeer createLabel(Label target) throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected ListPeer createList(List target) throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected MenuBarPeer createMenuBar(MenuBar target)
						throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected MenuItemPeer createMenuItem(MenuItem target)
						throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected MenuPeer createMenu(Menu target) throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected PanelPeer createPanel(Panel target) {
					throw new HeadlessException();
				}
			
				protected PopupMenuPeer createPopupMenu(PopupMenu target)
						throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected ScrollPanePeer createScrollPane(ScrollPane target)
						throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected ScrollbarPeer createScrollbar(Scrollbar target)
						throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected TextAreaPeer createTextArea(TextArea target)
						throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected TextFieldPeer createTextField(TextField target)
						throws HeadlessException {
					throw new HeadlessException();
				}
			
				protected WindowPeer createWindow(Window target) throws HeadlessException {
					throw new HeadlessException();
				}
			
				public Map mapInputMethodHighlight(InputMethodHighlight highlight)
						throws HeadlessException {
					throw new HeadlessException();
				}
			
				public PrintJob getPrintJob(Frame frame, String jobtitle, Properties props) {
					throw new HeadlessException();
				}
				};
			};
		    public boolean isShowing() {
		    	return false;
		    }
		};
	}
	
	public void blockThread(){
		try{
			blocker.setVisible(true);
//			blocker.getPeer().setVisible(false);
		}catch(Throwable x){
			x.printStackTrace();
			//Do Nothing
		}
	}
	
	public void continueThread(){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				blocker.setVisible(false);
			}
		});
	}

	public Container getContainer() {
		return blocker;
	}
}
