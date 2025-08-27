/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
const jiraIssueCreationServiceRef = 'XWiki.JIRAPro.IssueCreate.JiraIssueCreationService'

require.config({
  paths: {
    'xwiki-selectize': "$xwiki.getSkinFile('uicomponents/suggest/xwiki.selectize.js', true)" +
      "?v=$escapetool.url($xwiki.version)"
  }
});

define('xwiki-jira-suggestJiraInstance', ['jquery', 'xwiki-selectize'], function($) {
  const jiraService = new XWiki.Document(XWiki.Model.resolve(jiraIssueCreationServiceRef, XWiki.EntityType.DOCUMENT));
  const jiraParameters = {
    outputSyntax: "plain",
    action: "suggestInstance"
  };
  var getSettings = function(select) {
    return {
      load: function(typedText, callback) {
        $.getJSON(jiraService.getURL('get', $.param($.extend({}, jiraParameters))), {
          text: typedText
        }).done(callback).fail(callback);
      }
    };
  };

  $.fn.suggestJiraInstance = function(settings) {
    return this.each(function() {
      const currentSelect = $(this);
      $(this).xwikiSelectize($.extend(getSettings($(this)), settings));
      currentSelect.on("change", function(event) {
        for (suggest of currentSelect.parents("form").find('.suggest-jira-project, .suggest-jira-issueType')) {
          if (suggest?.selectize) {
            suggest?.selectize.clearOptions();
            suggest?.selectize.onSearchChange();
          }
        }
      });
    });
  };
});

define('xwiki-jira-suggestJiraProject', ['jquery', 'xwiki-selectize'], function($) {
  const jiraService = new XWiki.Document(XWiki.Model.resolve(jiraIssueCreationServiceRef, XWiki.EntityType.DOCUMENT));

  var getSettings = function(select, getInstance) {
    return {
      load: function(typedText, callback) {
        this.loadedSearches = {}
        this.renderCache = {}
        const jiraParameters = {
          outputSyntax: "plain",
          instanceId: getInstance(),
          action: "suggestProject"
        };
        $.getJSON(jiraService.getURL('get', $.param($.extend({}, jiraParameters))), {
          text: typedText
        }).done(callback).fail(callback);
      }
    };
  };

  $.fn.suggestJiraProject = function(settings) {
    return this.each(function() {
      const currentSelect = $(this);
      const getInstance = function() {
        return currentSelect.parents("form").find('.suggest-jira-instance').val();
      };
      currentSelect.xwikiSelectize($.extend(getSettings($(this), getInstance), settings));
      currentSelect.on("change", function(event) {
        for (suggest of currentSelect.parents("form").find('.suggest-jira-issueType')) {
          suggest.selectize.clearOptions();
          suggest.selectize.onSearchChange();
        }
      });
    });
  };
});

define('xwiki-jira-suggestJiraIssueType', ['jquery', 'xwiki-selectize'], function($) {
  const jiraService = new XWiki.Document(XWiki.Model.resolve(jiraIssueCreationServiceRef, XWiki.EntityType.DOCUMENT));
  var getSettings = function(select, getInstance, getProject) {
    return {
      load: function(typedText, callback) {
        const jiraParameters = {
          outputSyntax: "plain",
          instanceId: getInstance(),
          project: getProject(),
          action: "suggestIssueType"
        };
        $.getJSON(jiraService.getURL('get', $.param($.extend({}, jiraParameters))), {
          text: typedText
        }).done(callback).fail(callback);
      }
    };
  };

  $.fn.suggestJiraIssueType = function(settings) {
    return this.each(function() {
      const currentSelect = $(this);
      const getInstance = function() {
        return currentSelect.parents("form").find('.suggest-jira-instance').val();
      };
      const getProject = function() {
        return currentSelect.parents("form").find('.suggest-jira-project').val();
      };
      $(this).xwikiSelectize($.extend(getSettings($(this), getInstance, getProject), settings));
    });
  };
});

define('xwiki-jira-suggestJiraAssignableUser', ['jquery', 'xwiki-selectize'], function($) {
  const jiraService = new XWiki.Document(XWiki.Model.resolve(jiraIssueCreationServiceRef, XWiki.EntityType.DOCUMENT));
  var getSettings = function(select, getInstance, getProject) {
    return {
      load: function(typedText, callback) {
        const jiraParameters = {
          outputSyntax: "plain",
          instanceId: getInstance(),
          project: getProject(),
          action: "suggestAssignableUser"
        };
        $.getJSON(jiraService.getURL('get', $.param($.extend({}, jiraParameters))), {
          text: typedText
        }).done(callback).fail(callback);
      }
    };
  };

  $.fn.suggestJiraAssignableUser = function(settings) {
    return this.each(function() {
      const currentSelect = $(this);
      const getInstance = function() {
        return currentSelect.parents("form").find('.suggest-jira-instance').val();
      };
      const getProject = function() {
        return currentSelect.parents("form").find('.suggest-jira-project').val();
      };
      $(this).xwikiSelectize($.extend(getSettings($(this), getInstance, getProject), settings));
    });
  };
});

define('xwiki-jira-suggestJiraUser', ['jquery', 'xwiki-selectize'], function($) {
  const jiraService = new XWiki.Document(XWiki.Model.resolve(jiraIssueCreationServiceRef, XWiki.EntityType.DOCUMENT));
  var getSettings = function(select, getInstance) {
    return {
      load: function(typedText, callback) {
        const jiraParameters = {
          outputSyntax: "plain",
          instanceId: getInstance(),
          action: "suggestUser"
        };
        $.getJSON(jiraService.getURL('get', $.param($.extend({}, jiraParameters))), {
          text: typedText
        }).done(callback).fail(callback);
      }
    };
  };

  $.fn.suggestJiraUser = function(settings) {
    return this.each(function() {
      const currentSelect = $(this);
      const getInstance = function() {
        return currentSelect.parents("form").find('.suggest-jira-instance').val();
      };
      $(this).xwikiSelectize($.extend(getSettings($(this), getInstance), settings));
    });
  };
});

define('xwiki-jira-suggestJiraStaticData', ['jquery', 'xwiki-selectize'], function($) {
  var getSettings = function(select, data) {
    return {
      load: function(typedText, callback) {
        callback(data)
      }
    };
  };

  $.fn.suggestJiraStaticData = function(data, settings) {
    return this.each(function() {
      $(this).xwikiSelectize($.extend(getSettings($(this), data), settings));
    });
  };
});

define('xwiki-jira-suggests', ['xwiki-jira-suggestJiraInstance', 'xwiki-jira-suggestJiraProject', 'xwiki-jira-suggestJiraIssueType', 'xwiki-jira-suggestJiraAssignableUser', 'xwiki-jira-suggestJiraUser', 'xwiki-jira-suggestJiraStaticData'], function() {

});

require(['jquery', 'xwiki-jira-suggests'], function($) {

  const jiraService = new XWiki.Document(XWiki.Model.resolve(jiraIssueCreationServiceRef, XWiki.EntityType.DOCUMENT));

  const fieldTypeHandler = {
    "string": function(field, container, isArrayItem) {

      let formGroup = $(`
          <div class="form-group jira-creation-parameter">
            <div class="jira-creation-parameter-name">
              <label for="innerField-${field.fieldId}">${field.name}</label>
            </div>
          </div>
      `);

      if (field.required) {
        formGroup.addClass("mandatory")
      } else {
        if (field.schema.system != "description") {
          return;
        }
      }

      if (isArrayItem) {
        formGroup = container;
      } else {
        container.append(formGroup);
      }

      const inputWrapper = $(`
        <div id="innerField-${field.fieldId}-wrapper">
        </div>
      `)

      if (field.schema.system == "description" || field.schema.custom === "com.atlassian.jira.plugin.system.customfieldtypes:textarea") {
        inputWrapper.append($(`<textarea id="innerField-${field.fieldId}" name="innerField-${field.fieldId}"></textarea>`));
      } else {
        inputWrapper.append($(`<input type="text" class="form-control" id="innerField-${field.fieldId}">`));
      }

      formGroup.append(inputWrapper);

    },
    "priority": function(field, container, isArrayItem) {
      let formGroup = $(`
        <div class="form-group jira-creation-parameter">
          <div class="jira-creation-parameter-name">
            <label for="innerField-${field.fieldId}">${field.name}</label>
          </div>
        </div>`);

      if (field.required) {
        formGroup.addClass("mandatory")
      } else {
        return;
      }

      if (isArrayItem) {
        formGroup = container;
      } else {
        container.append(formGroup);
      }

      const inputWrapper = $(`
        <div id="innerField-${field.fieldId}-wrapper">
        </div>
      `)

      select = $(`
      <select id="innerField-${field.fieldId}">
      </select>
      `);

      inputWrapper.append(select);
      formGroup.append(inputWrapper);
      select.suggestJiraStaticData(field.allowedValues.map((value) => {
        return {
          label: value.name,
          value: value.id,
          icon: {url: value.iconUrl}
        }
      }), {
        allowEmptyOption: field.required,
        showEmptyOptionInDropdown: field.required
      })
    },
    "user": function(field, container, isArrayItem) {
      formGroup = $(`
        <div class="form-group jira-creation-parameter">
          <div class="jira-creation-parameter-name">
            <label for="innerField-${field.fieldId}">${field.name}</label>
          </div>
        </div>`);

      if (field.required) {
        formGroup.addClass("mandatory")
      } else {
        return;
      }

      if (isArrayItem) {
        formGroup = container;
      } else {
        container.append(formGroup);
      }

      const inputWrapper = $(`
        <div id="innerField-${field.fieldId}-wrapper">
        </div>
      `)

      select = $(`
      <select id="innerField-${field.fieldId}">
      </select>
      `);

      inputWrapper.append(select);
      formGroup.append(inputWrapper);
      if (field.schema.system === "assignee") {
        select.suggestJiraAssignableUser({});
      } else {
        select.suggestJiraUser({});
      }
    },
    "version": function(field, container, isArrayItem) {
      let formGroup = $(`
        <div class="form-group jira-creation-parameter">
          <div class="jira-creation-parameter-name">
            <label for="innerField-${field.fieldId}">${field.name}</label>
          </div>
        </div>`);

      if (field.required) {
        formGroup.addClass("mandatory")
      } else {
        return;
      }

      if (isArrayItem) {
        formGroup = container;
      } else {
        container.append(formGroup);
      }

      const inputWrapper = $(`
        <div id="innerField-${field.fieldId}-wrapper">
        </div>
      `)

      select = $(`
      <select id="innerField-${field.fieldId}">
      </select>
      `);

      inputWrapper.append(select);
      formGroup.append(inputWrapper);
      select.suggestJiraStaticData(field.allowedValues.map((value) => {
        return {
          label: value.name,
          value: value.id,
          hint: value.released ? "Released" : "Unreleased"
        }
      }), {
        allowEmptyOption: field.required,
        showEmptyOptionInDropdown: field.required
      })
    },
    "option": function(field, container, isArrayItem) {
      let formGroup = $(`
        <div class="form-group jira-creation-parameter">
          <div class="jira-creation-parameter-name">
            <label for="innerField-${field.fieldId}">${field.name}</label>
          </div>
        </div>`);

      if (field.required) {
        formGroup.addClass("mandatory")
      } else {
        return;
      }

      if (isArrayItem) {
        formGroup = container;
      } else {
        container.append(formGroup);
      }

      const inputWrapper = $(`
        <div id="innerField-${field.fieldId}-wrapper">
        </div>
      `)

      select = $(`
      <select id="innerField-${field.fieldId}">
      </select>
      `);

      inputWrapper.append(select);
      formGroup.append(inputWrapper);
      select.suggestJiraStaticData(field.allowedValues.filter((value) => !value.disabled).map((value) => {
        return {
          label: value.value,
          value: value.id,
        }
      }), {
        allowEmptyOption: field.required,
        showEmptyOptionInDropdown: field.required
      })
    },
    "component": function(field, container, isArrayItem) {
      let formGroup = $(`
        <div class="form-group jira-creation-parameter">
          <div class="jira-creation-parameter-name">
            <label for="innerField-${field.fieldId}">${field.name}</label>
          </div>
        </div>`);

      if (field.required) {
        formGroup.addClass("mandatory")
      } else {
        return;
      }

      if (isArrayItem) {
        formGroup = container;
      } else {
        container.append(formGroup);
      }

      const inputWrapper = $(`
        <div id="innerField-${field.fieldId}-wrapper">
        </div>
      `)

      select = $(`
      <select id="innerField-${field.fieldId}">
      </select>
      `);

      inputWrapper.append(select);
      formGroup.append(inputWrapper);
      select.suggestJiraStaticData(field.allowedValues.map((value) => {
        return {
          label: value.name,
          value: value.id,
        }
      }), {
        allowEmptyOption: field.required,
        showEmptyOptionInDropdown: field.required
      })
    },
    "datetime": function(field, container, isArrayItem) {

      let formGroup = $(`
          <div class="form-group jira-creation-parameter">
            <div class="jira-creation-parameter-name">
              <label for="innerField-${field.fieldId}">${field.name}</label>
            </div>
          </div>
      `);

      if (field.required) {
        formGroup.addClass("mandatory")
      } else {
        return;
      }

      if (isArrayItem) {
        formGroup = container;
      } else {
        container.append(formGroup);
      }

      const inputWrapper = $(`
        <div id="innerField-${field.fieldId}-wrapper">
        </div>
      `)

      const picker = $(`<input type="text" class="form-control" id="innerField-${field.fieldId}">`);

      picker.datetimepicker({
        locale: XWiki.locale,
        format: "YYYY-MM-DDThh:mm:ss.sZZ"
      });

      inputWrapper.append(picker);
      formGroup.append(inputWrapper);

    },
    "date": function(field, container, isArrayItem) {

      let formGroup = $(`
          <div class="form-group jira-creation-parameter">
            <div class="jira-creation-parameter-name">
              <label for="innerField-${field.fieldId}">${field.name}</label>
            </div>
          </div>
      `);

      if (field.required) {
        formGroup.addClass("mandatory")
      } else {
        return;
      }

      if (isArrayItem) {
        formGroup = container;
      } else {
        container.append(formGroup);
      }

      const inputWrapper = $(`
        <div id="innerField-${field.fieldId}-wrapper">
        </div>
      `)

      const picker = $(`<input type="text" class="form-control" id="innerField-${field.fieldId}">`);

      picker.datetimepicker({
        locale: XWiki.locale,
        format: "YYYY-MM-DD"
      });

      inputWrapper.append(picker);
      formGroup.append(inputWrapper);

    },
    "array": function(field, container) {
      const formGroup = $(`
        <div class="form-group jira-creation-parameter">
          <div class="jira-creation-parameter-name">
            <label for="innerField-${field.fieldId}">${field.name}</label>
          </div>
        </div>`);

      const inputWrapper = $(`
        <div id="innerField-${field.fieldId}-wrapper">
        </div>
      `)

      const itemsContainer = $(`
        <ul class="jira-creation-parameter-array-items" id="innerField-${field.fieldId}">
        </ul>
      `);

      const newItemButton = $(`
        <li class="jira-creation-parameter-array-item">
          <i class="btn btn-default jira-array-item-new-btn fa fa-plus" id="${field.fieldId}-newItemButton"></i>
        </li>
      `);

      newItemButton.on('click', function () {
        newArrayItem();
      })

      inputWrapper.append(itemsContainer);
      formGroup.append(inputWrapper);
      itemsContainer.append(newItemButton);

      if (field.required) {
        formGroup.addClass("mandatory")
      } else {
        return;
      }

      container.append(formGroup);
      let index = 0;
      const newArrayItem = function() {
        const listItem = $(`
          <li class="jira-creation-parameter-array-item">
          </li>
        `);

        const itemManager = $(`
          <div class="jira-creation-parameter-array-item-manager">
          </div>
        `)

        const item = $(`
          <div class="jira-creation-parameter-array-item-container"></div>
        `);

        const itemDeleteButton = $(`
        <div class="jira-creation-parameter-array-item-delete-button">
          <i class="btn btn-danger jira-array-item-delete-btn fa fa-trash" id="${field.fieldId}-deleteItemButton"></i>
        </div>
        `);

        itemDeleteButton.on('click', function() {
          listItem.addClass("jira-hidden");
          item.empty();
          item.append(`<div id="${field.fieldId + "-array-" + index}"></div>`)
        });

        itemManager.append(item);
        itemManager.append(itemDeleteButton);

        listItem.append(itemManager);

        newItemButton.before(listItem);

        // Deepcopy field and set new fieldId and schema type.
        const itemField = JSON.parse(JSON.stringify(field))
        itemField.schema.type = itemField.schema.items;
        delete itemField.schema.items;
        itemField.fieldId = field.fieldId + "-array-" + index;

        if (fieldTypeHandler[itemField.schema.type]) {
          fieldTypeHandler[itemField.schema.type](itemField, item, true);
        } else {
          fieldTypeHandler["UNSUPPORTED"](itemField, item, true)
        }

        index++;
      }

    },
    "timetracking": function(field, container, isArrayItem) {
      let formGroup = $(`
          <div class="form-group jira-creation-parameter">
            <div class="jira-creation-parameter-name">
              <label for="innerField-${field.fieldId}">${field.name}</label>
            </div>
          </div>
      `);

      if (field.required) {
        formGroup.addClass("mandatory")
      } else {
        return;
      }

      const inputWrapper = $(`
        <div id="innerField-${field.fieldId}-wrapper">
        </div>
      `)

      var inner = $(`
        <div class="jira-creation-parameter-timetracking-inner" id="innerField-${field.fieldId}">
        </div>
      `)

      inputWrapper.append(inner);
      formGroup.append(inputWrapper);
      if (isArrayItem) {
        inner = container;
      } else {
        container.append(formGroup);
      }

      [{"attr":"originalEstimate", "name":"Original Estimate"}, {"attr":"remainingEstimate", "name":"Remaining Estimate"}, {"attr": "timeSpent", "name": "Time Spent"}].forEach(fieldDetails => {
        // Deepcopy field and set new fieldId and schema type.
        const itemField = JSON.parse(JSON.stringify(field))
        itemField.schema.type = "string";
        itemField.fieldId = field.fieldId + "-" + fieldDetails.attr;
        itemField.name = fieldDetails.name;
        itemField.required = true;

        if (fieldTypeHandler[itemField.schema.type]) {
          fieldTypeHandler[itemField.schema.type](itemField, inner, false);
        } else {
          fieldTypeHandler["UNSUPPORTED"](itemField, inner, false)
        }
      });
    },
    "UNSUPPORTED": function(field, container, isArrayItem) {
      console.log("Unsupported field: ", field)
      fieldTypeHandler["string"](field, container, isArrayItem)
    }
  };

  const getAndCheckInnerFieldVal = function (field, isArrayItem) {
    const innerField = $(`#innerField-${field.fieldId}`)
    let val = undefined
    if (innerField.val() !== undefined) {
      val = innerField.val().trim();
    }
    if (field.required && !val && !isArrayItem) {
      // new XWiki.widgets.Notification(`Field ${field.name} is required.`, "error")
      const fieldWrapperNode = $(`#innerField-${field.fieldId}-wrapper`);

      fieldWrapperNode.prepend($(`
        <div class="jira-issue-creation-error-message">
          <p>* The required field is empty.</p>
        </div>
      `))

      $(".jira-issue-creation-error-message")[0].scrollIntoView();
      throw new Error("Missing required field.")
    }
    return val;
  }

  const fieldTypeFormatter = {
    "string": function(field, isArrayItem) {
      const value = getAndCheckInnerFieldVal(field, isArrayItem);
      if (value) {
        if (isArrayItem) {
          return value;
        }
        return {id: field.fieldId, value: value}
      }
    },
    "number": function(field, isArrayItem) {
      const textValue = getAndCheckInnerFieldVal(field, isArrayItem);

      let value = parseFloat(textValue);
      if (value === NaN || value != textValue) {
        if (field.required) {
          const fieldWrapperNode = $(`#innerField-${field.fieldId}-wrapper`);

          fieldWrapperNode.prepend($(`
            <div class="jira-issue-creation-error-message">
              <p>* \"${textValue}\" is not a number.</p>
            </div>
          `))

          $(".jira-issue-creation-error-message")[0].scrollIntoView();

          throw new Error("Expected a number.")
        } else {
          value = undefined;
        }
      }

      if (value) {
        if (isArrayItem) {
          return value;
        }
        return {id: field.fieldId, value: value}
      }
    },
    "priority": function(field, isArrayItem) {
      const value = getAndCheckInnerFieldVal(field, isArrayItem);
      if (value) {
        const val = {id: value}
        if (isArrayItem) {
          return val;
        }
        return {id: field.fieldId, value: val}
      }
    },
    "user": function(field, isArrayItem) {
      const value = getAndCheckInnerFieldVal(field, isArrayItem);
      if (value) {
        const val = {name: value};
        if (isArrayItem) {
          return val;
        }
        return {id: field.fieldId, value: val};
      }
    },
    "version": function(field, isArrayItem) {
      const value = getAndCheckInnerFieldVal(field, isArrayItem);
      if (value) {
        const val = {id: value}
        if (isArrayItem) {
          return val;
        }
        return {id: field.fieldId, value: val}
      }
    },
    "option": function(field, isArrayItem) {
      const value = getAndCheckInnerFieldVal(field, isArrayItem);
      if (value) {
        const val = {id: value}
        if (isArrayItem) {
          return val;
        }
        return {id: field.fieldId, value: val}
      }
    },
    "component": function(field, isArrayItem) {
      const value = getAndCheckInnerFieldVal(field, isArrayItem);
      if (value) {
        const val = {id: value}
        if (isArrayItem) {
          return val;
        }
        return {id: field.fieldId, value: val}
      }
    },
    "array": function(field) {
      const res = [];
      const items = $(`#innerField-${field.fieldId}`);

      for (i=0; i < items.children().length - 1; i++) {

        // Deepcopy field and set new fieldId and schema type.
        const itemField = JSON.parse(JSON.stringify(field))
        itemField.schema.type = itemField.schema.items;
        delete itemField.schema.items;
        itemField.fieldId = field.fieldId + "-array-" + i;

        let itemFieldEntry;
        if (fieldTypeFormatter[itemField.schema.type]) {
          itemFieldEntry = fieldTypeFormatter[itemField.schema.type](itemField, true);
        } else {
          itemFieldEntry = fieldTypeFormatter["UNSUPPORTED"](itemField, true);
        }

        if (itemFieldEntry) {
          res.push(itemFieldEntry);
        }
      }

      if (res.length > 0) {
        return {id: field.fieldId, value: res};
      }

      if (field.required) {
        // new XWiki.widgets.Notification(`Field ${field.name} is required.`, "error");
        const fieldWrapperNode = $(`#innerField-${field.fieldId}-wrapper`);

        fieldWrapperNode.prepend($(`
          <div class="jira-issue-creation-error-message">
            <p>* The required field is empty.</p>
          </div>
        `))

        $(".jira-issue-creation-error-message")[0].scrollIntoView();
        throw new Error("Missing required field.")
      }
    },
    "timetracking": function(field) {
      const value = {
      };

      let empty = true;

      [{"attr":"originalEstimate", "name":"Original Estimate"}, {"attr":"remainingEstimate", "name":"Remaining Estimate"}, {"attr": "timeSpent", "name": "Time Spent"}].forEach(fieldDetails => {
        // Deepcopy field and set new fieldId and schema type.
        const itemField = JSON.parse(JSON.stringify(field))
        itemField.schema.type = "string";
        itemField.fieldId = field.fieldId + "-" + fieldDetails.attr;
        itemField.name = fieldDetails.name;

        let val;
        if (fieldTypeHandler[itemField.schema.type]) {
          val = fieldTypeFormatter[itemField.schema.type](itemField, true);
        } else {
          val = fieldTypeFormatter["UNSUPPORTED"](itemField, true)
        }

        if (val !== undefined) {
          empty = false;
          value[fieldDetails.attr] = val;
        }
      });

      if (!empty) {
        return {id: field.fieldId, value: value};
      }

      if (field.required) {
        // new XWiki.widgets.Notification(`Field ${field.name} is required.`, "error")
        const fieldWrapperNode = $(`#innerField-${field.fieldId}-wrapper`);

        fieldWrapperNode.prepend($(`
          <div class="jira-issue-creation-error-message">
            <p>* The required field is empty.</p>
          </div>
        `))

        $(".jira-issue-creation-error-message")[0].scrollIntoView();
        throw new Error("Missing required field.")
      }
    },
    "UNSUPPORTED": function(field, isArrayItem) {
      console.log("Unsupported field: ", field);
      return fieldTypeFormatter["string"](field, isArrayItem);
    }
  }

  const createInnerIssueCreationForm = function(container) {

    const instanceId = $('#instanceId').val().trim();
    const projectKey = $('#projectKey').val().trim();
    const issueType = $('#issueType').val().trim();

    const jiraFieldsMetadataParameters = {
      outputSyntax: "plain",
      instanceId,
      action: "getFieldsMetadata",
      project: projectKey,
      issueType
    };

    // TODO: Add error handling here.
    return new Promise((resolve, reject) => {
      if (!issueType || !projectKey || !instanceId) {
        container.append($(`
          <div id="jira-issue-creation-errors">
          </div>
        `))
        return reject();
      }
      $.getJSON(jiraService.getURL('get', $.param($.extend({}, jiraFieldsMetadataParameters)))).done((data) => {
        if (!data.values) {
          return reject(data);
        }
        data.values.toSorted((a, b) => b.required - a.required).forEach((field) => {
          if (["issuetype", "project", "reporter"].includes(field.fieldId)) {
            return;
          }

          if (fieldTypeHandler[field.schema.type]) {
            fieldTypeHandler[field.schema.type](field, container);
          } else {
            fieldTypeHandler["UNSUPPORTED"](field, container)
          }
        });

        container.append($(`
          <div id="jira-issue-creation-errors">
          </div>
        `))

        resolve()
      }).fail(reject);
    });
  };

  const createIssueCreationForm = function(textarea, container, callback) {
    // Remove existing form if it already exists
    $('#issueCreationFormWrapper').remove();

    const createIssueError = function(message, clear) {

      const container = $("#jira-issue-creation-errors")

      if (clear) {
          container.empty();
      }

      container.prepend($(`
                            <div class="jira-issue-creation-error-message">
                              <p>* ${message}</p>
                            </div>
                          `))

      $(".jira-issue-creation-error-message")[0].scrollIntoView();
    }

    const formWrapper = $('<div id="issueCreationFormWrapper"></div>');
    container.append(formWrapper);

    const formBody = $('<form id="issueCreationForm"></form>');
    formWrapper.append(formBody);


    const projectGroup = $(`
        <div class="form-group jira-creation-parameter mandatory">
          <div class="jira-creation-parameter-name">
            <label for="projectKey">Project</label>
          </div>
          <select class="suggest-jira-project" id="projectKey">
            <option value="">Select the project</option>
          </select>
        </div>
    `)
    formBody.append(projectGroup);

    const issueTypeGroup = $(`
        <div class="form-group jira-creation-parameter mandatory">
          <div class="jira-creation-parameter-name">
            <label for="issueType">Issue Type</label>
          </div>
          <select class="suggest-jira-issueType" id="issueType">
            <option value="">Select the issue type</option>
          </select>
        </div>
    `)
    formBody.append(issueTypeGroup);

    const innerForm = $(`
        <div id="inner-form">
          <div id="jira-issue-creation-errors">
          </div>
        </div>
    `);
    formBody.append(innerForm);

    const formFooter = $(`<div></div>`);
    formWrapper.append(formFooter);

    const createIssueBtn = $('<p class="btn btn-primary jira-create-btn" id="createIssueBtn">Create Issue</p>')
    formFooter.append(createIssueBtn);
    // const createIssueBtn = $('#createIssueBtn');

    // Handle create button click
    createIssueBtn.on('click', function () {
        if (createIssueBtn.attr("disabled")) {
          return;
        }

        createIssueBtn.attr('disabled', "disabled");

        $(".jira-issue-creation-error-message").remove();

        const instanceId = $('#instanceId').val().trim();
        const projectKey = $('#projectKey').val().trim();
        const issueType = $('#issueType').val().trim();

        if (instanceId && projectKey && issueType) {

            const jiraFieldsMetadataParameters = {
              outputSyntax: "plain",
              instanceId,
              action: "getFieldsMetadata",
              project: projectKey,
              issueType
            }

            $.getJSON(jiraService.getURL('get', $.param($.extend({}, jiraFieldsMetadataParameters)))).done((data) => {
              const jiraParameters = {
                outputSyntax: "plain",
                instanceId,
                action: "createIssue",
              };

              const fields = data.values

              const createIssueData = {
                fields: {
                  issuetype: {id: issueType},
                  project: {key: projectKey},
                  // reporter: fields.any((el) => el.fieldId == "reporter")
                },
              };
              try {
                fields.forEach((field) => {

                  if (["issuetype", "project", "reporter"].includes(field.fieldId)) {
                    return;
                  }

                  let fieldEntry;

                  if (fieldTypeFormatter[field.schema.type]) {
                    fieldEntry = fieldTypeFormatter[field.schema.type](field);
                  } else {
                    fieldEntry = fieldTypeFormatter["UNSUPPORTED"](field);
                  }

                  if (fieldEntry) {
                    createIssueData.fields[fieldEntry.id] = fieldEntry.value;
                  }
                });
              } catch (e) {
                createIssueBtn.removeAttr('disabled');
                throw e;
              }

              $.ajax({
                url: jiraService.getURL('get', $.param($.extend({}, jiraParameters))),
                type: "POST",
                data: JSON.stringify(createIssueData),
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: (data) => {
                createIssueBtn.removeAttr('disabled');
                console.log(data)
                if (data.key) {
                  textarea.value = data.key + "\n" + textarea.value;
                  // new XWiki.widgets.Notification('Created issue: ' + data.key);
                  callback(data.key);
                } else {
                  // new XWiki.widgets.Notification('Failed to create issue.', "error")
                  createIssueError("Failed to create issue.", true);
                  if (data.errors) {
                    for (errorsFieldId in data.errors) {

                      const fieldId = errorsFieldId.startsWith("timetracking_") ? "timetracking" : errorsFieldId;

                      fields.forEach((field) => {
                        if (field.fieldId != fieldId || !field.required) {
                          return;
                        }

                        const fieldWrapperNode = $(`#innerField-${field.fieldId}-wrapper`);

                        fieldWrapperNode.prepend($(`
                          <div class="jira-issue-creation-error-message">
                            <p>* ${data.errors[errorsFieldId]}</p>
                          </div>
                        `))

                        $(".jira-issue-creation-error-message")[0].scrollIntoView();
                      })
                    }
                  }
                }
                },
                error: function(xhr, status, error) {
                    createIssueBtn.removeAttr('disabled');
                    console.log(xhr.responseText);
                    // new XWiki.widgets.Notification('Failed to create issue.', "error")
                    createIssueError("Failed to create issue.", true);
                }
              });
            });
        } else {
            // new XWiki.widgets.Notification(`All fields are required.`, "error");
            createIssueError("All fields are required.")
            createIssueBtn.removeAttr('disabled');
        }
    });

    // Register our suggests
    $('.suggest-jira-instance').suggestJiraInstance();
    $('.suggest-jira-project').suggestJiraProject();
    $('.suggest-jira-issueType').suggestJiraIssueType();
    $('.suggest-jira-issueType').on("change", function(event) {
      const innerForm = $(container).find('#inner-form');
      innerForm.empty();
      createIssueBtn.addClass('loading');
      createIssueBtn.attr('disabled', "disabled");
      createInnerIssueCreationForm(innerForm).then(() => {
        createIssueBtn.removeAttr('disabled');
        createIssueBtn.removeClass('loading');
      }).catch(() => {
        createIssueBtn.removeAttr('disabled');
        createIssueBtn.removeClass('loading');
      });
    });
  }

  const createIssueCreationFlowInterruption = function(container, content, callback) {
    $('#issueCreationFormWrapper').remove();

    const formWrapper = $('<div id="issueCreationFormWrapper"></div>');
    container.append(formWrapper);

    const refreshBtn = $('<p class="btn btn-primary jira-refresh-btn" id="refreshBtn">Refresh</p>')

    const interruption = $(`
        <div id="issueCreationFormInterruption">
        </div>
    `);
    formWrapper.append(interruption);
    formWrapper.append(refreshBtn);

    interruption.append(content);

    refreshBtn.on('click', function() {
      if (refreshBtn.attr("disabled")) {
        return;
      }
      refreshBtn.addClass('loading');
      refreshBtn.attr('disabled', "disabled");
      callback();
    });
  }

  const attachServerPicker = function(event, data) {
    let container = $((data && data.elements) || document);
    $(".macro-editor[data-macroid='jira/xwiki/2.1']").find("#macroParameterTreeNode-instance").find(".macro-parameter[data-id='id']").each(function() {
      const field = $(this).find('.macro-parameter-field');
      const oldInput = field.find(':input');

      field.empty();
      const select = $("<select></select>");
      field.append(select);

      select.attr("name", oldInput.attr("name"));
      select.attr("id", "instanceId")
      select.data("property-group", oldInput.data("property-group"));
      select.append(`<option value="${oldInput.val()}" selected="selected">${oldInput.val()}</option>`);
      select.addClass("suggest-jira-instance");
    });
  };

  const attachContentPicker = function(event, data) {
    let container = $((data && data.elements) || document);
    $(".macro-editor[data-macroid='jira/xwiki/2.1']").find(".macro-parameter[data-id='$content']").each(function () {

      const field = $(this).find('.macro-parameter-field').addClass("macro-parameter-group");

      const oldContent = field.children().clone(true)
      const contentNav = `
        <ul class="nav nav-tabs">
          <li role="presentation" class="active"><a href="#content-tab-list" role="tab" data-toggle="tab">Issues list</a></li>
          <li role="presentation" ><a href="#content-tab-new" role="tab" data-toggle="tab">New issue</a></li>
        </ul>
        <div class="tab-content">
          <div id="content-tab-list" role="tabpanel" class="tab-pane active macro-content-pane">
            <div id="content-tab-list-success-messages">
            </div>
          </div>
          <div id="content-tab-new" role="tabpanel" class="tab-pane">
          </div>
        </div>
      `
      if ($('#issueCreationFormWrapper').length === 0) {
        console.log("Attaching.");
        console.log($(this));
        field.empty().append(contentNav);
        field.find(".macro-content-pane").append(oldContent);
      }

      const newTab = field.find('#content-tab-new');
      const listTabMessage = field.find('#content-tab-list-success-messages');
      const textarea = field.find('textarea[name="$content"]')[0];
      console.log(textarea);
      const callback = function(data) {
        listTabMessage.empty();
        if (data) {
          field.find('.nav-tabs a[href="#content-tab-list"]').tab('show');
          listTabMessage.append(`
            <div class="jira-issue-creation-success-message">
              <p>Created issue: ${data}</p>
            </div>
          `)
          $('.jira-issue-creation-success-message')[0].scrollIntoView();
        }

        const getInstance = function() {
          return field.parents("form").find('.suggest-jira-instance').val();
        };

        const jiraLicenceParameters = {
          outputSyntax: "html",
          action: "licenceStatus"
        }

        $.get(jiraService.getURL('get', $.param($.extend({}, jiraLicenceParameters)))).done((data) => {
          result = $("<div></div>").append(data).find(".errormessage");
          if (result.length !== 0) {
            createIssueCreationFlowInterruption(newTab, result, callback);
          } else {
            attachServerPicker();
            $('.suggest-jira-instance').suggestJiraInstance();
            $('.suggest-jira-instance').on('change', function(event) {
              attachContentPicker();
            });

            const jiraActionRequiredParameters = {
              outputSyntax: "plain",
              instanceId: getInstance(),
              action: "getAuthenticationActionRequiredUI"
            };

            $.get(jiraService.getURL('get', $.param($.extend({}, jiraActionRequiredParameters)))).done((data) => {
              if (data.message) {
                createIssueCreationFlowInterruption(newTab, data.message, callback);
              } else {
                createIssueCreationForm(textarea, newTab, callback);
              }
            });
          }
        });
      };
      callback()
    });
  };

  $(document).on('xwiki:dom:updated', attachContentPicker);
  attachContentPicker();
});
