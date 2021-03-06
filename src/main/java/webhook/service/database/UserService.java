package webhook.service.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.MessagingType;
import com.github.messenger4j.send.message.TextMessage;
import com.github.messenger4j.send.message.quickreply.QuickReply;
import com.github.messenger4j.send.message.quickreply.TextQuickReply;
import com.github.messenger4j.userprofile.UserProfile;

import webhook.controller.CallBackHandle;
import webhook.entity.User;
import webhook.repository.UserRepo;
import webhook.service.WebhookService;

@Service
public class UserService {

	@PersistenceContext protected EntityManager entityManager;
	
	@Autowired
	WebhookService webhookService;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private Messenger messenger;
	
	public void newUser(String id) throws MessengerApiException, MessengerIOException {
		UserProfile userProfile = this.messenger.queryUserProfile(id);
		System.out.println(userProfile.firstName());
		User user = new User(id, userProfile.firstName(), userProfile.lastName(), null, null, userProfile.profilePicture());
		user.setStatus("FREE");
		userRepo.save(user);
	}
	
	public List<User> findUser(String id) {
		String sql = "SELECT * FROM vannoichatbot.tbl_user WHERE id = "+id+";";
		Query query = entityManager.createNativeQuery(sql, User.class);
		return query.getResultList();
	}

	public List<User> findPartner(String partnerGender, String userGender) {
		String sql = "SELECT * FROM vannoichatbot.tbl_user WHERE status = 'FINDING' and gender = '" + partnerGender + "';";
		Query query = entityManager.createNativeQuery(sql, User.class);
		return query.getResultList();
	}

	public void sendSettingGender(String senderId) throws MessengerApiException, MessengerIOException {
		System.out.println("sendSettingGender");
        List<QuickReply> quickReplies = new ArrayList<>();

        quickReplies.add(TextQuickReply.create("Nam", "settingmale"));
        quickReplies.add(TextQuickReply.create("N???", "settingfemale"));

        TextMessage message = TextMessage.create("H??y cho bot bi???t gi???i t??nh c???a b???n !!\n\n????? c??i ?????t l???i gi???i t??nh c???a m??nh chat /setting\n\nL??u ??: N???u b???n khai b??o sai gi???i t??nh c???a m??nh, b???n s??? b??? c???m chat!", Optional.of(quickReplies), Optional.empty());
        this.messenger.send(MessagePayload.create(senderId, MessagingType.RESPONSE, message));
	}

	public void settingGender(String senderId, String gender) {
		String saveGender = null;
		switch (gender.toLowerCase()) {
		case "settingmale":
			saveGender = "male";
			break;
		case "settingfemale":
			saveGender = "female";
			break;
		default:
			break;
		}
		User user = findUser(senderId).get(0);
		user.setGender(saveGender);
		userRepo.save(user);

        webhookService.sendTextMessage(senderId, "C??i ?????t gi???i t??nh th??nh c??ng !!\nB???n ???? s???n s??ng s??? d???ng chatbot\nChat /hd ????? ???????c h?????ng d???n ");
	}

}
