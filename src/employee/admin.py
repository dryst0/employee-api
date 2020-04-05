from django.contrib import admin

from .models import Employee

# Register your models here.
class EmployeeAdmin(admin.ModelAdmin):
    list_display = ('id', 'user', 'employee_type')
    readonly_fields = ('user_json_formatted',)

admin.site.register(Employee, EmployeeAdmin)