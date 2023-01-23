package mk.ukim.finki.wp.kol2022.g2.service.service.impl;

import mk.ukim.finki.wp.kol2022.g2.model.Course;
import mk.ukim.finki.wp.kol2022.g2.model.Student;
import mk.ukim.finki.wp.kol2022.g2.model.StudentType;
import mk.ukim.finki.wp.kol2022.g2.model.exceptions.InvalidCourseIdException;
import mk.ukim.finki.wp.kol2022.g2.model.exceptions.InvalidStudentIdException;
import mk.ukim.finki.wp.kol2022.g2.repository.CourseRepository;
import mk.ukim.finki.wp.kol2022.g2.repository.StudentRepository;
import mk.ukim.finki.wp.kol2022.g2.service.StudentService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StudentServiceImpl implements StudentService, UserDetailsService {
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseRepository courseRepository;

    public StudentServiceImpl(StudentRepository studentRepository, PasswordEncoder passwordEncoder, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.courseRepository = courseRepository;
    }

    @Override
    public List<Student> listAll() {
        return studentRepository.findAll();
    }

    @Override
    public Student findById(Long id) {
        return studentRepository.findById(id).orElseThrow(InvalidStudentIdException::new);
    }

    @Override
    public Student create(String name, String email, String password, StudentType type, List<Long> courseId, LocalDate enrollmentDate) {
        String encryptedPassword = passwordEncoder.encode(password);
        List<Course> courses = courseRepository.findAllById(courseId);
        Student student = new Student(name, email, encryptedPassword, type, courses, enrollmentDate);
        return studentRepository.save(student);
    }

    @Override
    public Student update(Long id, String name, String email, String password, StudentType type, List<Long> coursesId, LocalDate enrollmentDate) {
        String encryptedPassword = passwordEncoder.encode(password);

        Student student = this.findById(id);
        student.setName(name);
        student.setEmail(email);
        student.setPassword(encryptedPassword);
        student.setType(type);
        student.setCourses(courseRepository.findAllById(coursesId));
        student.setEnrollmentDate(enrollmentDate);
        return studentRepository.save(student);
    }

    @Override
    public Student delete(Long id) {
        Student student = this.findById(id);
        studentRepository.delete(student);
        return student;
    }

    @Override
    public List<Student> filter(Long courseId, Integer yearsOfStudying) {

        Course course = courseRepository.findById(courseId).orElseThrow(InvalidCourseIdException::new);

        return studentRepository.findAllByCoursesLikeAndEnrollmentDate_DayOfYearAndEnrollmentDateLessThanEqual(course,yearsOfStudying,2022);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Student student = this.studentRepository.findStudentByEmail(email).orElseThrow(InvalidStudentIdException::new);

        UserDetails userDetails = new org.springframework.security.core.userdetails.
                User(student.getEmail(), student.getPassword(),
                Stream.of(new SimpleGrantedAuthority(student.getType().toString()))
                        .collect(Collectors.toList()));

        return userDetails;
    }
}