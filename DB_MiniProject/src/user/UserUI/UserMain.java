package user.UserUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import DTO.BOOKS;
import DTO.BookDetailWrapper;
import DTO.RENTALS;
import DTO.RESERVATIONS;
import DTO.REVIEWS;
import user.DAO.DAO;
import user.DAO.LSH_DAO;

public class UserMain extends JFrame {
	public String userId;

    public UserMain(String userId) {
    	this.userId = userId;
        userMainWindow(); // 생성자에서 UI 설정 메서드 호출
    }

	LSH_DAO lsh_dao = new LSH_DAO();
	private JTextField searchField; // 검색 입력창
	private JButton searchButton; // 검색 버튼
	private JComboBox<String> bookCategory;
	private JPanel BookListPanel;
	private JPanel SearchPanel;
	private JPanel SearchDetailPanel; // SearchDetailPanel을 클래스 인스턴스 변수로 설정
	private String[] bookCategoryColumn = { "전체", "인문", "사회", "과학", "소설", "수필", "시" };
	private JTextField bookIdField;
	private JTextField bookNameField;
	private JTextField authorField;
	private JTextField publisherField;
	private JTextField releaseDateField;
	private JTextField categoryField;
	private JTextField stockField;
	private JTextArea summaryArea;

	public void userMainWindow() {
		setTitle("도서 관리 시스템");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 400);
		setLocationRelativeTo(null);

        // 내부 패널 생성 및 GridLayout 설정 (3x2 배열, 버튼 간격 30px)
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 30, 30));
        JButton bookList = new JButton("도서 목록");
        JButton userInfo = new JButton("내 정보");
        JButton rsvRtlList = new JButton("내 예약/대여");
        JButton rcmBook = new JButton("희망도서신청");
        
        gridPanel.add(bookList);
        gridPanel.add(userInfo);
        gridPanel.add(rsvRtlList);
        gridPanel.add(rcmBook);
        
        userInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openUserInfoDialog();
            }
        });
        rcmBook.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRecommendBookDialog();
            }
        });
        

        bookList.addActionListener(e -> BookList(userId));

        // "내 예약/대여" 버튼 클릭 시 동작
        rsvRtlList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showReservationsAndRentals();
            }
        });

        
        // 외부 패널 생성 및 여백 추가
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBorder(new EmptyBorder(50, 50, 50, 50)); // 여백 추가
        outerPanel.add(gridPanel, BorderLayout.CENTER); // 내부 패널 추가

		add(outerPanel);
		setVisible(true);
	}
	
	// 희망도서신청
    private void openRecommendBookDialog() {
        JDialog dialog = new JDialog(this, "희망도서 신청", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        RecommendBook recommendBookMain = new RecommendBook(userId);
        dialog.add(recommendBookMain);
        dialog.setVisible(true);
    }
    
    // 내 정보
    private void openUserInfoDialog() {
    	JDialog dialog = new JDialog(this, "내 정보", true);
        dialog.setSize(800, 700);
        dialog.setLocationRelativeTo(this);
        UserInfo userInfo = new UserInfo(userId);
        dialog.add(userInfo);
        dialog.setVisible(true);
    }
    
    // 내 예약/대여
    private void showReservationsAndRentals() {
        JFrame reservationFrame = new JFrame("내 예약 및 대여 목록");
        reservationFrame.setSize(800, 600);
        reservationFrame.setLocationRelativeTo(null);
        reservationFrame.setLayout(new BorderLayout()); // BorderLayout으로 설정

        // 대여 테이블
        String[] rentalColumnNames = {"대여 ID", "회원 ID", "도서 ID", "대여 날짜", "반납 예정일", "반납일", "상태"};
        DefaultTableModel rentalTableModel = new DefaultTableModel(rentalColumnNames, 0);

        DAO dao = new DAO(); // DAO 인스턴스 생성
        try {
            ResultSet rentalRs = dao.getRentals(); // 대여 데이터 가져오는 메서드 호출

            while (rentalRs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rentalRs.getInt("rentalId"));
                row.add(rentalRs.getString("userID"));
                row.add(rentalRs.getInt("bookID"));
                row.add(rentalRs.getDate("rentalDate"));
                row.add(rentalRs.getDate("returnDueDate"));
                row.add(rentalRs.getDate("returnDate"));
                row.add(rentalRs.getString("rentalState"));
                rentalTableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // 대여 테이블 생성
        JTable rentalTable = new JTable(rentalTableModel);
        JScrollPane rentalScrollPane = new JScrollPane(rentalTable);
        rentalScrollPane.setBorder(null); // JScrollPane의 여백 제거

        // 대여 제목 추가
        JPanel rentalPanel = new JPanel(new BorderLayout());
        rentalPanel.add(new JLabel("대여 목록", JLabel.CENTER), BorderLayout.NORTH);
        rentalPanel.add(rentalScrollPane, BorderLayout.CENTER);

        // 예약 테이블
        String[] reservationColumnNames = {"예약 ID", "회원 ID", "도서 ID", "예약 날짜", "상태"};
        DefaultTableModel reservationTableModel = new DefaultTableModel(reservationColumnNames, 0); // 예약 테이블 모델 초기화

        try {
            ResultSet reservationRs = dao.getReservations();

            while (reservationRs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(reservationRs.getInt("rsID"));
                row.add(reservationRs.getString("userID"));
                row.add(reservationRs.getInt("bookID"));
                row.add(reservationRs.getDate("rsDate"));
                row.add(reservationRs.getString("rsState"));
                reservationTableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // 예약 테이블 생성
        JTable reservationTable = new JTable(reservationTableModel);
        JScrollPane reservationScrollPane = new JScrollPane(reservationTable);
        reservationScrollPane.setBorder(null); // JScrollPane의 여백 제거

        // 예약 제목 추가
        JPanel reservationPanel = new JPanel(new BorderLayout());
        reservationPanel.add(new JLabel("예약 목록", JLabel.CENTER), BorderLayout.NORTH);
        reservationPanel.add(reservationScrollPane, BorderLayout.CENTER);

        // 삭제 버튼 추가
        JButton deleteButton = new JButton("예약 취소");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = reservationTable.getSelectedRow();
                if (selectedRow != -1) {
                    int reservationId = (int) reservationTableModel.getValueAt(selectedRow, 0); // 예약 ID 가져오기

                    // DAO를 통해 예약 삭제
                    if (dao.deleteReservation(reservationId)) {
                        reservationTableModel.removeRow(selectedRow); // 테이블에서 행 삭제
                        JOptionPane.showMessageDialog(reservationFrame, "예약한 도서가 삭제되었습니다.");
                    } else {
                        JOptionPane.showMessageDialog(reservationFrame, "예약 삭제에 실패했습니다.");
                    }
                } else {
                    JOptionPane.showMessageDialog(reservationFrame, "예약 취소 할 도서를 선택하세요.");
                }
            }
        });

        // 삭제 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.add(deleteButton);

        // 프레임에 대여와 예약 패널 추가
        JPanel mainPanel = new JPanel(new GridLayout(2, 1)); // 대여와 예약 테이블을 수직으로 배치
        mainPanel.add(rentalPanel);
        mainPanel.add(reservationPanel);

        reservationFrame.add(mainPanel, BorderLayout.CENTER); // 메인 패널은 중앙에 배치
        reservationFrame.add(buttonPanel, BorderLayout.SOUTH); // 버튼 패널을 하단에 배치

        reservationFrame.setVisible(true);

        dao.close(); // DAO 자원 해제
    }

    //도서 리스트 
	public void BookList(String userID) {
		JFrame BookListFrame = new JFrame("도서 목록");
		System.out.println(userID);
		BookListFrame.setSize(1000, 600);
		BookListFrame.setLocationRelativeTo(null);
		BookListFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		BookListPanel = new JPanel(new BorderLayout());

		SearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		SearchPanel.add(new JLabel("검색 범위"));
		bookCategory = new JComboBox<>(bookCategoryColumn);
		SearchPanel.add(bookCategory);
		searchField = new JTextField(20);
		SearchPanel.add(searchField);

		SearchDetailPanel = new JPanel(new BorderLayout());

		// 기본 검색 수행 (초기 테이블 데이터 로드)
		String initialCategory = (String) bookCategory.getSelectedItem();
		String initialSearchTerm = "";
		loadTableData(initialCategory, initialSearchTerm); 

		searchButton = new JButton("검색");
		searchButton.addActionListener(e -> {
			String searchTerm = searchField.getText();
			String category = (String) bookCategory.getSelectedItem();
			loadTableData(category, searchTerm); 
		});
		SearchPanel.add(searchButton);
		// Enter 키 이벤트로 검색 실행
		searchField.addActionListener(e -> {
			String searchTerm = searchField.getText();
			String category = (String) bookCategory.getSelectedItem();

			loadTableData(category, searchTerm);
		});

		SearchPanel.setPreferredSize(new Dimension(1000, 50));
		BookListPanel.add(SearchPanel, BorderLayout.NORTH);
		BookListPanel.add(SearchDetailPanel, BorderLayout.CENTER);

		BookListFrame.add(BookListPanel);
		BookListFrame.setVisible(true);
	}
	
	//도서 목록 테이블 생성
	private void loadTableData(String category, String searchTerm) {
		Object[][] data = lsh_dao.searchBooks(category, searchTerm);
		String[] columnNames = { "책번호", "책이름", "작가", "출판사", "출시일", "책카테고리", "재고" };

		DefaultTableModel model = new DefaultTableModel(data, columnNames) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JTable bookTable = new JTable(model);
		bookTable.setRowHeight(25);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		for (int i = 0; i < bookTable.getColumnCount(); i++) {
			bookTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		}

		// 테이블 더블 클릭시 이벤트 발생
		bookTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = bookTable.rowAtPoint(e.getPoint());

					
					Object selectedBook = bookTable.getValueAt(row, 0); 
					bookDetailWindow((int) selectedBook, userId);
				}
			}
		});

		SearchDetailPanel.removeAll();

		JScrollPane tableScrollPane = new JScrollPane(bookTable);
		tableScrollPane.setPreferredSize(new Dimension(1000, 500)); // 스크롤 패널 크기 조정
		tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // 세로 스크롤 항상 보이게

		SearchDetailPanel.add(tableScrollPane, BorderLayout.CENTER);
		SearchDetailPanel.revalidate();
		SearchDetailPanel.repaint();
	}

	//도서 상세 창
	public void bookDetailWindow(int bookId, String userID) {
		JFrame mainFrame = new JFrame("도서 상세 보기");
		mainFrame.setSize(600, 900); // Main frame size
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainFrame.setLocationRelativeTo(null); // Center the window
		
		
		JPanel backgroundPanel = new JPanel();
		backgroundPanel.setLayout(new BorderLayout());

		// 도서 상세 내용 표시
		JPanel bookDetailPanel = new JPanel();
		bookDetailPanel.setLayout(new GridBagLayout());
		bookDetailPanel.setPreferredSize(new Dimension(600, 550)); // Set content panel size

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10); 
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		BookDetailWrapper list = lsh_dao.bookdetail(bookId);
		BOOKS book = list.getBookDto();
		RENTALS rental =list.getRentalDto();
		RESERVATIONS rs = list.getReservationDto();
		REVIEWS rev = list.getReviewDto();
		
		int bookdetailID = book.getBookID();
		String bookID = Integer.toString(bookdetailID);
		String bookName = book.getBookName();
		String writer = book.getWriter();
		String publisher = book.getPublisher();
		String pubdate = book.getPubDate().toString();
		String bookcategory = book.getBookCTG();
		String stock = Integer.toString(book.getStock());
		String description = book.getDescription();
		
		String[] rentAvail = {rental.getRentalState() == null || rental.getRentalState().equals("반납") ? "대여 가능" : "대여 불가능"};
		String availRent1 = rentAvail[0];
		System.out.println(rental.getRentalState());
		System.out.println(availRent1);
		String returnDate = rental.getReturnDueDate() != null ? rental.getReturnDueDate().toString() : "-";
		
		
		String[] rsAvail = {rs.getRsState() == null || rs.getRsState().equals("Y")? "예약 가능" : "예약 불가능"};
		String availRs = rsAvail[0];
		System.out.println(rs.getRsState());
		System.out.println(availRs);
		

		JLabel bookIdLabel = new JLabel("책 번호: ");
		JLabel bookNameLabel = new JLabel("책 이름: ");
		JLabel authorLabel = new JLabel("저자: ");
		JLabel publisherLabel = new JLabel("출판사: ");
		JLabel releaseDateLabel = new JLabel("출시일: ");
		JLabel categoryLabel = new JLabel("책 카테고리: ");
		JLabel stockLabel = new JLabel("재고: ");
		JLabel summaryLabel = new JLabel("줄거리: ");
		JLabel availRent = new JLabel("대여가능 여부: ");
		JLabel availReserve = new JLabel("예약가능 여부: ");
		JLabel exReturnDate = new JLabel("반납 예정일: ");

		JTextField bookIdField = new JTextField(bookID);
		JTextField bookNameField = new JTextField(bookName);
		JTextField authorField = new JTextField(writer);
		JTextField publisherField = new JTextField(publisher);
		JTextField releaseDateField = new JTextField(pubdate);
		JTextField categoryField = new JTextField(bookcategory);
		JTextField stockField = new JTextField(stock);
		JTextArea summaryArea = new JTextArea(8, 40);
		JTextField availRentField = new JTextField(availRent1);
		JTextField availReserveField = new JTextField(availRs);
		JTextField exReturnDateField = new JTextField(returnDate);
		
		summaryArea.setText(description);
		summaryArea.setWrapStyleWord(true);
		summaryArea.setLineWrap(true);
		summaryArea.setCaretPosition(0);
		summaryArea.setEditable(false); 

		bookIdField.setBackground(null);
		bookIdField.setBorder(null);
		bookNameField.setBackground(null);
		bookNameField.setBorder(null);
		authorField.setBackground(null);
		authorField.setBorder(null);
		publisherField.setBackground(null);
		publisherField.setBorder(null);
		releaseDateField.setBackground(null);
		releaseDateField.setBorder(null);
		categoryField.setBackground(null);
		categoryField.setBorder(null);
		stockField.setBackground(null);
		stockField.setBorder(null);
		summaryArea.setBorder(null);
		summaryArea.setBackground(null);
		availRentField.setBackground(null);
		availRentField.setBorder(null);
		availReserveField.setBackground(null);
		availReserveField.setBorder(null);
		exReturnDateField.setBackground(null);
		exReturnDateField.setBorder(null);
		
		JButton rentBtn = new JButton("대여하기");
		JButton reserveBtn = new JButton("예약하기");
		rentBtn.addActionListener(e -> rentBook(userID,bookdetailID,availRent1));
		reserveBtn.addActionListener(e -> RevervationBook(userId,bookdetailID,availRs,availRent1));
		

		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		bookDetailPanel.add(bookIdLabel, gbc);
		gbc.gridx = 1;
		bookDetailPanel.add(bookIdField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		bookDetailPanel.add(bookNameLabel, gbc);
		gbc.gridx = 1;
		bookDetailPanel.add(bookNameField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		bookDetailPanel.add(authorLabel, gbc);
		gbc.gridx = 1;
		bookDetailPanel.add(authorField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		bookDetailPanel.add(publisherLabel, gbc);
		gbc.gridx = 1;
		bookDetailPanel.add(publisherField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		bookDetailPanel.add(releaseDateLabel, gbc);
		gbc.gridx = 1;
		bookDetailPanel.add(releaseDateField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 5;
		bookDetailPanel.add(categoryLabel, gbc);
		gbc.gridx = 1;
		bookDetailPanel.add(categoryField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 6;
		bookDetailPanel.add(stockLabel, gbc);
		gbc.gridx = 1;
		bookDetailPanel.add(stockField, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 7;
		bookDetailPanel.add(availRent, gbc);
		gbc.gridx = 1;
		bookDetailPanel.add(availRentField, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 8;
		bookDetailPanel.add(availReserve, gbc);
		gbc.gridx = 1;
		bookDetailPanel.add(availReserveField, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 9;
		bookDetailPanel.add(exReturnDate, gbc);
		gbc.gridx = 1;
		bookDetailPanel.add(exReturnDateField, gbc);

		
		gbc.gridx = 0;
		gbc.gridy = 10;
		bookDetailPanel.add(summaryLabel, gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		bookDetailPanel.add(new JScrollPane(summaryArea), gbc);
		backgroundPanel.add(bookDetailPanel, BorderLayout.NORTH);
		
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new FlowLayout());
		btnPanel.setPreferredSize(new Dimension(600, 50));
		btnPanel.add(rentBtn);
		btnPanel.add(reserveBtn);
		
		backgroundPanel.add(btnPanel, BorderLayout.CENTER);
		
		// 한줄평 패널
		JPanel commentPanel = new JPanel();
		commentPanel.setLayout(new GridBagLayout());
		commentPanel.setPreferredSize(new Dimension(600, 200));

		gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10); // Padding
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridwidth = 1;
		gbc.weightx = 0;
		JLabel commentLabel = new JLabel("한줄평: ");
		gbc.gridx = 0;
		gbc.gridy = 0;
		commentPanel.add(commentLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0; // 가로로 확장
		JTextField commentArea = new JTextField(20);
		commentPanel.add(commentArea, gbc);

		JLabel starLabel = new JLabel("별점");
		gbc.weightx = 0;
		gbc.gridx = 2;
		commentPanel.add(starLabel, gbc);

		String[] star = { "5", "4", "3", "2", "1" };
		JComboBox<String> stars = new JComboBox<String>(star);
		gbc.gridx = 3;
		commentPanel.add(stars, gbc);
		JButton submitButton = new JButton("등록");
		gbc.gridx = 4;
		commentPanel.add(submitButton, gbc);
		
		
		submitButton.addActionListener(e -> {
		    // commentArea.getText()를 버튼 클릭 시마다 호출하여 사용자가 입력한 값 반영
		    String comment = (commentArea.getText() != null && !commentArea.getText().trim().isEmpty())
		                     ? commentArea.getText() 
		                     : "댓글없음"; // 텍스트가 없으면 "댓글없음"으로 처리

		    addcomment(userId, bookId, (String) stars.getSelectedItem(), comment);
		});

		// 댓글 테이블
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 5; // 전체 폭을 차지하도록 설정
		gbc.fill = GridBagConstraints.BOTH; // 테이블이 패널의 남은 공간을 채우도록 설정
		gbc.weightx = 1.0; // 가로 확장
		gbc.weighty = 1.0; // 세로 확장

		// Table to display comments
		String[] columnNames = { "사용자명", "한줄평", "별점", "등록일" };
		Object[][] data = lsh_dao.searchReviews(bookId);
		JTable commentsTable = new JTable(data, columnNames);
		
		// Set column widths 600
		commentsTable.getColumnModel().getColumn(0).setPreferredWidth(50); // 사용자명
		commentsTable.getColumnModel().getColumn(1).setPreferredWidth(300); // 한줄평
		commentsTable.getColumnModel().getColumn(2).setPreferredWidth(50);  // 별점
		commentsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // 등록일

		// Center-align text in all columns
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER); // 가운데 정렬

		for (int i = 0; i < commentsTable.getColumnCount(); i++) {
		    commentsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		}
		
		JScrollPane tableScrollPane = new JScrollPane(commentsTable);
		commentsTable.setRowHeight(25);
		commentPanel.add(tableScrollPane, gbc);

		backgroundPanel.add(commentPanel, BorderLayout.SOUTH);
		mainFrame.add(backgroundPanel);
		mainFrame.setVisible(true);
	}
	
	//도서 대여
	public void rentBook(String userID, int bookID,String rentAvail) {
		if(rentAvail.equals("대여 불가능")) {
			JOptionPane.showMessageDialog(null, "현재 대여 불가 상태입니다.", "대여 불가", JOptionPane.ERROR_MESSAGE);
			return; // 메서드 종료
		}
		int result = lsh_dao.rentalBooks(userID, bookID) ;
		System.out.println("결과"+result);
		if(result ==1) {
			JOptionPane.showMessageDialog(this, "대여 성공했습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
	}
	
	//도서 예약
	public void RevervationBook(String userID, int bookID,String reservationAvail, String rentAvail ) {
		if(rentAvail.equals("대여 가능")) {
			JOptionPane.showMessageDialog(null, "현재 대여 가능 상태입니다. 대여를 진행해주세요.", "예약 불가", JOptionPane.ERROR_MESSAGE);
			return; // 메서드 종료
		}
		if(reservationAvail.equals("예약 불가능")) {
			JOptionPane.showMessageDialog(null, "현재 예약 불가 상태입니다.", "예약 불가", JOptionPane.ERROR_MESSAGE);
			return; // 메서드 종료
		}
		int result = lsh_dao.RevervationBook(userID, bookID) ;
		System.out.println("예약 결과"+result);
		if(result ==1) {
			JOptionPane.showMessageDialog(this, "예약 성공했습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
	}
	
	//한줄평 추가 
	public void addcomment(String userId, int bookId, String stars, String review) {
		System.out.println("star:"+stars);
		System.out.println("review"+review);
		if(review.equals("댓글없음")) {
			JOptionPane.showMessageDialog(null, "한줄평을 입력하세요!.", "입력 오류", JOptionPane.ERROR_MESSAGE);
			return; // 메서드 종료
		}
		int result = lsh_dao.addReviews(userId, bookId, stars, review);
		if(result ==1) {
			JOptionPane.showMessageDialog(this, "한줄평을 달았습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
	}
	
}
