package kyonggi.bookslyserver.domain.shop.service;

import kyonggi.bookslyserver.domain.event.entity.Event;
import kyonggi.bookslyserver.domain.event.entity.closeEvent.ClosingEvent;
import kyonggi.bookslyserver.domain.event.entity.timeEvent.TimeEvent;
import kyonggi.bookslyserver.domain.event.entity.timeEvent.TimeEventSchedule;
import kyonggi.bookslyserver.domain.reservation.entity.ReservationSchedule;
import kyonggi.bookslyserver.domain.reservation.repository.ReservationScheduleRepository;
import kyonggi.bookslyserver.domain.shop.dto.request.employee.EmployeeCreateRequestDto;
import kyonggi.bookslyserver.domain.shop.dto.request.employee.EmployeeWorkScheduleDto;
import kyonggi.bookslyserver.domain.shop.dto.response.employee.*;
import kyonggi.bookslyserver.domain.shop.entity.Employee.Employee;
import kyonggi.bookslyserver.domain.shop.entity.Employee.EmployeeMenu;
import kyonggi.bookslyserver.domain.shop.entity.Employee.WorkSchedule;
import kyonggi.bookslyserver.domain.shop.entity.Menu.Menu;
import kyonggi.bookslyserver.domain.shop.entity.Shop.Shop;
import kyonggi.bookslyserver.domain.shop.repository.*;
import kyonggi.bookslyserver.global.aws.s3.AmazonS3Manager;
import kyonggi.bookslyserver.global.common.uuid.Uuid;
import kyonggi.bookslyserver.global.common.uuid.UuidService;
import kyonggi.bookslyserver.global.error.ErrorCode;
import kyonggi.bookslyserver.global.error.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static kyonggi.bookslyserver.global.error.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final EmployeeMenuRepository employeeMenuRepository;

    private final WorkScheduleRepository workScheduleRepository;

    private final ShopRepository shopRepository;

    private final MenuRepository menuRepository;

    private final ReservationScheduleRepository reservationScheduleRepository;

    private final UuidService uuidService;

    private final AmazonS3Manager amazonS3Manager;

    public List<EmployeeReadDto> readEmployee(Long id, Boolean withReviews){
        Shop shop = shopRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(SHOP_NOT_FOUND));


        List<EmployeeReadDto> employeeReadDtos = new ArrayList<>();

            if (shop.getEmployees().size() != 0) {
                for (Employee employee : shop.getEmployees()) {
                    EmployeeReadDto employeeReadDto = new EmployeeReadDto(employee, withReviews);
                    employeeReadDtos.add(employeeReadDto);
                }
            }
            else{
                throw new BusinessException(ErrorCode.EMPLOYEES_NOT_FOUND);
            }


        return employeeReadDtos;
    }


    public EmployeeReadOneDto readOneEmployee(Long id){
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(EMPLOYEES_NOT_FOUND));

        List<EmployeeMenu> employeeMenus = employeeMenuRepository.findByEmployeeId(id);


        return new EmployeeReadOneDto(employee, employeeMenus);
    }

    public List<ReserveEmployeesDto> readReserveEmployees(Long id){
        Shop shop = shopRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(SHOP_NOT_FOUND));

        if(shop.getEmployees() == null){
            throw new BusinessException(ErrorCode.RESERVE_EMPLOYEES_NOT_FOUND);
        }

        List<ReserveEmployeesDto> result = new ArrayList<>();

        for(Employee employee : shop.getEmployees()){
            result.add(new ReserveEmployeesDto(employee));
        }

        return result;
    }

    public List<EventRegisterEmployeeNamesDto> readEmployeeNames(Long id){
        List<Employee> employees = employeeRepository.findEmployeesByShopId(id);
        List<EventRegisterEmployeeNamesDto> result = employees.stream().map(employee -> new EventRegisterEmployeeNamesDto(employee)).collect(Collectors.toList());
        return result;
    }

    private void validateDuplicatedEmployeeName(EmployeeCreateRequestDto requestDto) {
        if (employeeRepository.existsByName(requestDto.name()))
            throw new ConflictException(ErrorCode.EMPLOYEE_NAME_DUPLICATE);
    }

    private void validateWorkSchedules(EmployeeCreateRequestDto requestDto) {
        if (requestDto.workSchedules().size() != 7) throw new InvalidValueException(WORK_SCHEDULE_MUST_SEVEN_DAYS);
    }

    private String uploadEmployeeImageToS3(MultipartFile imageFile) {
        Uuid uuid = uuidService.createUuid();
        return amazonS3Manager.uploadFile(amazonS3Manager.generateEmployeeKeyName(uuid), imageFile);
    }

    private void createEmployeeMenu(Employee employee, List<Menu> menus) {
        menus.forEach(menu -> {
            EmployeeMenu employeeMenu = EmployeeMenu.builder()
                    .menu(menu)
                    .employee(employee).build();
            employeeMenuRepository.save(employeeMenu);
        });
    }

    private void assignAllMenusToEmployee(Shop shop, Employee employee) {
        List<Menu> menusByShop = menuRepository.findMenusByShop(shop);
        createEmployeeMenu(employee, menusByShop);
    }

    private void assignSpecificMenusToEmployee(EmployeeCreateRequestDto requestDto, Employee employee) {
        List<Menu> menus = requestDto.menus().stream()
                .map(menuId -> menuRepository.findById(menuId).orElseThrow(() -> new EntityNotFoundException(MENU_NOT_FOUND)))
                .collect(Collectors.toList());
        createEmployeeMenu(employee, menus);
    }

    public EmployeeCreateResponseDto join(Long shopId, Boolean assignAllMenus,EmployeeCreateRequestDto requestDto){
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new EntityNotFoundException(SHOP_NOT_FOUND));
        validateDuplicatedEmployeeName(requestDto);
        validateWorkSchedules(requestDto);

        Employee employee = Employee.builder()
                .name(requestDto.name())
                .selfIntro(requestDto.description())
                .profileImgUri(requestDto.image() != null ? uploadEmployeeImageToS3(requestDto.image()) : null)
                .build();

        if (assignAllMenus) {
            assignAllMenusToEmployee(shop, employee);
        } else {
            assignSpecificMenusToEmployee(requestDto, employee);
        }

        //근무 스케쥴 생성
        requestDto.workSchedules().stream().forEach(workScheduleDto -> {
            WorkSchedule workSchedule = WorkSchedule.createEntity(workScheduleDto.dayOfWeek(), workScheduleDto.startTime(), workScheduleDto.endTime(), workScheduleDto.isDayOff(), employee);
            workScheduleRepository.save(workSchedule);
        });

        employeeRepository.save(employee);
        return new EmployeeCreateResponseDto(employee);
    }


    @Transactional
    public EmployeeUpdateResponseDto update(Long id, EmployeeCreateRequestDto requestDto){
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND));
        List<String> menus = new ArrayList<>();


        List<EmployeeMenu> employeeMenuList = employeeMenuRepository.findByEmployeeId(id);
        List<WorkSchedule> workScheduleList = workScheduleRepository.findByEmployeeId(id);

        for(EmployeeMenu employeeMenu : employeeMenuList){
            employeeMenuRepository.delete(employeeMenu);
        }

        for(WorkSchedule workSchedule : workScheduleList){
            workScheduleRepository.delete(workSchedule);
        }

        if(requestDto.menus() != null){
            for(Long menuId : requestDto.menus()){
                Optional<Menu> menu = menuRepository.findById(menuId);
                if(!menu.isPresent()){
                    throw new EntityNotFoundException(MENU_NOT_FOUND);
                }
                EmployeeMenu employeeMenu = employee.addMenu(employee, menu.get());
                employeeMenuRepository.save(employeeMenu);
                menus.add(menu.get().getMenuName());
            }
        }


        for(EmployeeWorkScheduleDto employeeWorkScheduleDto : requestDto.workSchedules()){
            WorkSchedule workSchedule = WorkSchedule.createEntity(employee, employeeWorkScheduleDto);
            employee.getWorkSchedules().add(workSchedule);
            workScheduleRepository.save(workSchedule);
        }

        employee.update(requestDto);

        return new EmployeeUpdateResponseDto(requestDto, menus);
    }


    @Transactional
    public EmployeeDeleteResponseDto delete(Long id){
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND));


        Shop shop = employee.getShop();
        shop.getEmployees().remove(employee);
        employeeRepository.deleteById(id);
        return new EmployeeDeleteResponseDto(id);
    }

    public GetCalendarDatesResponseDto getCalendarDates(Long shopId, Long employeeId) {

        shopRepository.findById(shopId).orElseThrow(() -> new EntityNotFoundException(SHOP_NOT_FOUND));
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND));

        if (employee.getShop().getId() != shopId) throw new ForbiddenException();

        LocalDate currentDate = LocalDate.now();
        List<WorkSchedule> workSchedules = employee.getWorkSchedules();
        int schedulingCycle = employee.getSchedulingCycle();
        LocalDate plusDate = currentDate .plusDays(schedulingCycle-1);

        List<LocalDate> workdays = new ArrayList<>();
        List<LocalDate> holidays = new ArrayList<>();

        Map<DayOfWeek,Boolean> workInfo = workSchedules.stream().collect(Collectors.toMap(WorkSchedule::getDayOfWeek,WorkSchedule::isDayOff));
        while (!currentDate.isAfter(plusDate)) {
            Boolean isDayOff = workInfo.get(currentDate .getDayOfWeek());
            if (isDayOff) holidays.add(currentDate );
            else workdays.add(currentDate );

            currentDate = currentDate.plusDays(1);
        }

        return GetCalendarDatesResponseDto.of(employee.getId(), workdays, holidays);
    }

    public Employee findEmployeeById(Long employeeId) {
        return employeeRepository
                .findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND));

    }

    public void validateBelongShop(Long employeeId, Long shopId) {
        if (!employeeRepository.existsByIdAndShopId(employeeId, shopId))
            throw new InvalidValueException(EMPLOYEE_NOT_BELONG_SHOP);
    }


    private void validateReservationScheduleBelongEmployee(Employee employee, ReservationSchedule reservationSchedule) {
        if (reservationSchedule.getEmployee().getId() != employee.getId()) {
            throw new InvalidValueException(EMPLOYEE_NOT_OWN_RESERVATION_SCHEDULE);
        }
    }

    @Transactional(readOnly = true)
    public CheckEventStatusResponseDto checkEventStatus(Long shopId, Long employeeId, Long reservationScheduleId) {
        Employee employee = findEmployeeById(employeeId);
        validateBelongShop(employeeId,shopId);

        ReservationSchedule reservationSchedule = reservationScheduleRepository.findById(reservationScheduleId).orElseThrow(() -> new EntityNotFoundException(RESERVATION_SCHEDULE_NOT_FOUND));
        validateReservationScheduleBelongEmployee(employee, reservationSchedule);
        boolean hasEvent = !reservationSchedule.isClosed() &&
                (reservationSchedule.isClosingEvent() || reservationSchedule.getTimeEventSchedule() != null);

        return CheckEventStatusResponseDto.of(hasEvent);
    }

    public List<Menu> findEmployeeClosingEventMenus(Employee employee, ClosingEvent closingEvent) {
        List<Menu> menus = closingEvent.getClosingEventMenus().stream()
                .map(closingEventMenu -> closingEventMenu.getMenu()).collect(Collectors.toList());
        return employeeMenuRepository.findByMenusAndEmployee(menus, employee);
    }

    public List<Menu> findEmployeeTimeEventMenus(Employee employee, TimeEvent timeEvent) {
        List<Menu> menus = timeEvent.getTimeEventMenus().stream()
                .map(timeEventMenu -> timeEventMenu.getMenu()).collect(Collectors.toList());

        return employeeMenuRepository.findByMenusAndEmployee(menus, employee);
    }

    public GetEventMenusResponseDto getEventMenus(Long shopId, Long employeeId, Long reservationScheduleId) {
        Employee employee = findEmployeeById(employeeId);
        validateBelongShop(employeeId,shopId);

        ReservationSchedule reservationSchedule = reservationScheduleRepository.findById(reservationScheduleId).orElseThrow(() -> new EntityNotFoundException(RESERVATION_SCHEDULE_NOT_FOUND));
        validateReservationScheduleBelongEmployee(employee, reservationSchedule);

        Map<Event, List<Menu>> eventMenus = new HashMap<>();
        if (!reservationSchedule.isClosed() && reservationSchedule.isClosingEvent()) {
            ClosingEvent closingEvent = reservationSchedule.getClosingEvent();
            List<Menu> employeeClosingEventMenus = findEmployeeClosingEventMenus(employee, closingEvent);
            eventMenus.put(closingEvent, employeeClosingEventMenus);
        }

        if (reservationSchedule.getTimeEventSchedule() != null) {
            TimeEvent timeEvent = reservationSchedule.getTimeEventSchedule().getTimeEvent();
            List<Menu> employeeTimeEventMenus = findEmployeeTimeEventMenus(employee, timeEvent);
            eventMenus.put(timeEvent, employeeTimeEventMenus);
        }

        return GetEventMenusResponseDto.of(eventMenus);
    }
}
