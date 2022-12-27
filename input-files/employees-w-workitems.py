import json

def read_employees(input_file: str):

    # convert a specific file
    with open(input_file, 'r') as input:

        employees = []
        employee = None
        for line in [line.strip() for line in input.readlines()]:

            if line.startswith('EMP'):

                if employee:
                    # write current and reset
                    employees.append(employee)

                employee_data = line.split(',')
                employee = {
                    'id': employee_data[1],
                    'dept_id': employee_data[2],
                    'title': employee_data[3],
                    'name': employee_data[4],
                    'birth_date': employee_data[5]
                }

            elif line.startswith('WRK'):

                wrk_data = line.split(',')
                wrk_json = {
                    'id': wrk_data[1],
                    'title': wrk_data[2]
                }

                key = 'work_items'
                if not key in employee:
                    employee[key] = []
                    
                employee[key].append(wrk_json)

        return employees

def write_employees(output_file: str, employees):

    with open(output_file, 'w') as out:
        json.dump(employees, out, indent = 4)


input_file = 'employees-w-workitems.csv'
output_file = ''.join([input_file[: -3], 'json'])

# convert the workitems from csv for json modules
employees = read_employees(input_file)
write_employees(output_file, employees)