package leare.apiGateway.controllers.graphql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;

import leare.apiGateway.controllers.consumers.AuthConsumer;
import leare.apiGateway.controllers.consumers.CourseConsumer;
import leare.apiGateway.controllers.consumers.DocumentConsumer;
import leare.apiGateway.controllers.consumers.SearchConsumer;
import leare.apiGateway.models.AuthModels.DecryptedToken;
import leare.apiGateway.models.CoursesModels.Category;
import leare.apiGateway.models.CoursesModels.Course;
import leare.apiGateway.models.CoursesModels.CourseByCategory;
import leare.apiGateway.models.CoursesModels.CourseModule;
import leare.apiGateway.models.CoursesModels.CreateCourseInput;
import leare.apiGateway.models.CoursesModels.CreateModuleInput;
import leare.apiGateway.models.CoursesModels.CreateSectionInput;
import leare.apiGateway.models.CoursesModels.EditCategoryInput;
import leare.apiGateway.models.CoursesModels.EditCourseInput;
import leare.apiGateway.models.CoursesModels.EditModuleInput;
import leare.apiGateway.models.CoursesModels.EditSectionInput;
import leare.apiGateway.models.CoursesModels.ModuleSection;
import leare.apiGateway.models.UserModels.Users;

import java.util.ArrayList;
import java.util.List;
@Controller
public class CoursesResolver {
    private final CourseConsumer coursesConsumer;
    private final SearchConsumer searchConsumer;
    private final DocumentConsumer documentConsumer;
    private final AuthConsumer authConsumer;

    @Autowired
    public CoursesResolver(CourseConsumer coursesConsumer, SearchConsumer searchConsumer, DocumentConsumer documentConsumer, AuthConsumer authConsumer) {
        this.coursesConsumer = coursesConsumer;
        this.searchConsumer = searchConsumer;
        this.documentConsumer = documentConsumer;
        this.authConsumer = authConsumer;
    }

    // CATEGORY

    @QueryMapping
    public Category[] categories(@ContextValue("Authorization") String AuthorizationHeader) throws Exception {
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }
        return coursesConsumer.getCategories();
    }

    @QueryMapping
    public Category categoryById(@Argument String id, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {

        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }

        return coursesConsumer.getCategoryById(id);
    }

    @MutationMapping
    public Category createCategory(@Argument String category_name, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {

        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }

        Category category = coursesConsumer.createCategory(category_name);
        searchConsumer.AddCategoryIndex(category.getCategory_id(), category.getCategory_name());

        return category;
    }

    @MutationMapping
    public Category editCategory(@Argument EditCategoryInput input, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {

        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }

        Category category = coursesConsumer.editCategory(input);
        searchConsumer.UpdateCategoryIndex(category.getCategory_id(), category.getCategory_name());

        return category;
    }
    
    @MutationMapping
    public Boolean deleteCategory(@Argument String id, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {

        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }

        coursesConsumer.deleteCategory(id);
        searchConsumer.DeleteIndex(id);
        
        return true;
    }

    // COURSE

    @QueryMapping
    public CourseByCategory[] coursesByCategory(@Argument("categories") List<String> categories, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }
        CourseByCategory[] courses = coursesConsumer.getCoursesByCategory(categories);

        courses = documentConsumer.updatePictureLink(courses);

        return courses;
    }

    @QueryMapping
    public Course[] listCourses(@Argument int page, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }
        Course[] courses = coursesConsumer.listCourses(page);
        courses = documentConsumer.updatePictureLink(courses);
        return courses;
    }

    @QueryMapping
    public Course courseById(@Argument String id, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }
        Course course = coursesConsumer.getCourseById(id);
        course = documentConsumer.updatePictureLink(course);
        course.setModules(course.getModules());

        return course;

    }

    @MutationMapping
    public Course createCourse(@Argument CreateCourseInput input, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {

        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }

        Course course = coursesConsumer.createCourse(input);
        course = documentConsumer.updatePictureLink(course);
        searchConsumer.AddCourseIndex(course.getCourse_id(), course.getCourse_name(), course.getCourse_description(), course.getPicture_id());

        return course;

    }

    @MutationMapping
    public Course editCourse(@Argument EditCourseInput input, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        DecryptedToken token = authConsumer.DecryptToken(AuthorizationHeader);

        if (!Auth || token==null || (!token.getRole().equals("admin") && token.getUserID()!=input.getCreator_id())) {
            throw new Exception("Auth Problem");
        }
        Course course = coursesConsumer.editCourse(input);
        course = documentConsumer.updatePictureLink(course);
        searchConsumer.UpdateCourseIndex(course.getCourse_id(), course.getCourse_description(), course.getCourse_name(), course.getPicture_id());
        return course;
    }

    @MutationMapping
    public Boolean deleteCourse(@Argument String id, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        DecryptedToken token = authConsumer.DecryptToken(AuthorizationHeader);

        Course course = coursesConsumer.getCourseById(id);

        if (!Auth || token==null || (!token.getRole().equals("admin") && token.getUserID()!=course.getCreator_id())) {
            throw new Exception("Auth Problem");
        }
        coursesConsumer.deleteCourse(id);
        documentConsumer.deleteDocument(token.getUsername(),id);
        searchConsumer.DeleteIndex(id);
        return true;
    }

    // MODULE

    @QueryMapping
    public CourseModule[] courseModules(@Argument String course_id, @Argument int page, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }
        return coursesConsumer.getCourseModules(course_id, page);
    }

    @QueryMapping
    public CourseModule moduleById(@Argument String id, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }
        return coursesConsumer.getModuleById(id);
    }

    @MutationMapping
    public CourseModule createModule(@Argument CreateModuleInput input, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);
        
        DecryptedToken token = authConsumer.DecryptToken(AuthorizationHeader);

        Course course = coursesConsumer.getCourseById(input.getCourse_id());

        if (!Auth || token==null || (!token.getRole().equals("admin") && token.getUserID()!=course.getCreator_id())) {
            throw new Exception("Auth Problem");
        }

        return coursesConsumer.createModule(input);
    }

    @MutationMapping
    public CourseModule editModule(@Argument EditModuleInput input, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);
        
        if (!Auth) {
            throw new Exception("Auth Problem");
        }
        return coursesConsumer.editModule(input);
    }

    @MutationMapping
    public Boolean deleteModule(@Argument String id, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }
        coursesConsumer.deleteModule(id);
        return true;
    }

    // SECTION

    // Funciones auxiliares para obtener los links de los archivos y videos de las secciones
    private ModuleSection updateSectionLinks(ModuleSection section) { //TODO: QUE EL MICROSERVICIO SE ENCARGUE DE ESTO
        if (section != null && section.getVideo_id() != null) {
            String link = documentConsumer.getDocument(section.getVideo_id()).getValue().getFilePath();
            section.setVideo_id(link);
            
            if (section.getFiles_array() != null) {

                List<String> files_links = new ArrayList<String>();

                for (String file_id : section.getFiles_array()) {
                    String file_link = documentConsumer.getDocument(file_id).getValue().getFilePath();
                    files_links.add(file_link);
                }

                section.setFiles_array(files_links);
            }
        }
        return section;
    }

    private ModuleSection[] updateSectionLinks(ModuleSection[] sections) { //TODO: QUE EL MICROSERVICIO SE ENCARGUE DE ESTO
        for (ModuleSection section : sections) {
            this.updateSectionLinks(section);
        }
        return sections;
    }

    @QueryMapping
    public ModuleSection[] moduleSections(@Argument String module_id, @Argument int page, @ContextValue("Authorization") String AuthorizationHeader) throws Exception { 
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }
        ModuleSection[] sections = coursesConsumer.getModuleSections(module_id, page);
        sections = this.updateSectionLinks(sections);
        return sections;
    }

    @QueryMapping
    public ModuleSection sectionById(@Argument String id, @ContextValue("Authorization") String AuthorizationHeader) throws Exception { 
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }
        ModuleSection section = coursesConsumer.getSectionById(id);
        section = this.updateSectionLinks(section);
        return section;
    }

    @MutationMapping
    public ModuleSection createSection(@Argument CreateSectionInput input, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }
        ModuleSection section = coursesConsumer.createSection(input);
        section = this.updateSectionLinks(section);
        return section;
    }

    @MutationMapping
    public ModuleSection editSection(@Argument EditSectionInput input, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }
        ModuleSection section = coursesConsumer.editSection(input);
        section = this.updateSectionLinks(section);
        return section;
    }

    @MutationMapping
    public Boolean deleteSection(@Argument String id, @ContextValue("Authorization") String AuthorizationHeader) throws Exception {
        
        Boolean Auth = authConsumer.CheckRoute("/users/:id", "patch", AuthorizationHeader);

        if (!Auth) {
            throw new Exception("Auth Problem");
        }

        ModuleSection section = coursesConsumer.getSectionById(id); // Se debe hacer antes de borrar para obtener los links de los archivos y borrarlos

        coursesConsumer.deleteSection(id);

        if (section != null && section.getVideo_id() != null) {//TODO: QUE EL MICROSERVICIO SE ENCARGUE DE ESTO 

            //ACTUALMENTE NO FUNCIONA. TOCA MANDAR USUARIO Y ID
            // documentConsumer.deleteDocument(section.getVideo_id());
            
            // if (section.getFiles_array() != null) {
            //     for (String file_id : section.getFiles_array()) {
            //         documentConsumer.deleteDocument(file_id);
            //     }
            // }
        }
        return true;
    }
}