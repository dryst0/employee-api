import json

from uuid import uuid4

from django.contrib.postgres.fields import JSONField
from django.db.models import Model, CharField, UUIDField
from django.utils.safestring import mark_safe

from pygments import highlight
from pygments.formatters.html import HtmlFormatter
from pygments.lexers.data import JsonLexer
from simple_history.models import HistoricalRecords


# Create your models here.
class Employee(Model):
    WORKER = "worker"
    MANAGER = "manager"
    FINANCE_MANAGER = "finance_manager"
    EMPLOYEE_TYPE_CHOICES = (
        (WORKER, WORKER),
        (MANAGER, MANAGER),
        (FINANCE_MANAGER, FINANCE_MANAGER),
    )

    id = UUIDField(primary_key=True, default=uuid4, editable=False)
    user = JSONField(blank=False, null=False)
    employee_type = CharField(
        max_length=15,
        choices=EMPLOYEE_TYPE_CHOICES,
        default=WORKER,
        db_index=True,
        db_column="type",
    )
    history = HistoricalRecords()

    def user_json_formatted(self):
        data = json.dumps(self.user, indent=4)
        formatter = HtmlFormatter(style="colorful")
        response = highlight(data, JsonLexer(), formatter)
        style = f"<style>{formatter.get_style_defs()}</style><br/>"

        return mark_safe(f"{style}{response}")
