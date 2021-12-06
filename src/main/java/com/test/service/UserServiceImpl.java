package com.test.service;

import com.test.exceptions.BadRequestException;
import com.test.exceptions.NotFoundException;
import com.test.model.Address;
import com.test.model.Status;
import com.test.model.User;
import com.test.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressService addressService;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAll() {

        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void save(User user) throws NotFoundException {
        user.setStatus(Status.UNVERIFIED);
        Address address = user.getAddress();
        addressService.save(address);
        String encodePasswrod = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePasswrod);
        userRepository.save(user);
    }

    @Override
    public void update(int id, String name) throws NotFoundException {

        Optional<User> optionalUser = userRepository.findById(Integer.valueOf(id));
        User u = optionalUser.get();
        u.setName(name);
        userRepository.save(u);
    }

    @Override
    public void delete(int id) {
        userRepository.deleteById(Integer.valueOf(id));
    }

    @Override
    public User getById(int id) throws NotFoundException {
        Optional<User> optionalUser = userRepository.findById(Integer.valueOf(id));
        if (!optionalUser.isPresent()) {
            throw new NotFoundException("Not found!");
        }
        return optionalUser.get();
    }

    @Override
    public User findByEmail(String email) throws NotFoundException{
        return userRepository.findByEmail(email);
    }

    @Override
    public User getByName(String name) {
        return userRepository.getByName(name);
    }

    @Override
    public User login(String email, String password) throws BadRequestException {
        User user = userRepository.getByEmailAndPassword(email,password);
        String status = String.valueOf(user.getStatus());

        if(!user.equals(null)) {
            if (status.equals("VERIFIED")){
                return user;
            }else {
                throw new BadRequestException("BadRequestException");
            }
        }else {
            throw new BadRequestException("BadRequestException");
        }
    }

    @Override
    public void sendEmail(String to, String subject, String text) throws NotFoundException, MessagingException {
            mailSender.sendSimpleMessage(to, subject, text);

    }

    @Override
    public void verified(String email) {
        User user;
        try {
            user = findByEmail(email);
            user.setStatus(Status.VERIFIED);
            userRepository.save(user);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

    }
}
